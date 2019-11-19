/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.{AssociateUCRRequest, Consolidation, DisassociateDUCRRequest, ShutMUCRRequest}
import forms._
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache.{AssociateUcrAnswers, DisassociateUcrAnswers, ShutMucrAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import repositories.CacheRepository
import services.audit.AuditService
import testdata.CommonTestData._
import testdata.MovementsTestData
import uk.gov.hmrc.http.HeaderCarrier
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val metrics = mock[MovementsMetrics]
  private val audit = mock[AuditService]
  private val repository = mock[CacheRepository]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val movementBuilder = mock[MovementBuilder]
  private val service = new SubmissionService(repository, connector, audit, metrics, movementBuilder)

  override def afterEach(): Unit = {
    reset(audit, connector, metrics, repository)
    super.afterEach()
  }

  "Submit Associate" should {
    val mucr = correctUcr_2
    val ucr = correctUcr

    "delegate to connector" in {

      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))
      given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

      val answers = AssociateUcrAnswers(Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      await(service.submit("eori", answers))

      theAssociationSubmitted mustBe AssociateUCRRequest("eori", mucr, ucr)
      verify(repository).removeByEori("eori")
      verify(audit).auditAssociate("eori", mucr, ucr, "Success")
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = AssociateUcrAnswers(Some(MucrOptions(mucr)), Some(AssociateUcr(AssociateKind.Ducr, ucr)))
      intercept[RuntimeException] {
        await(service.submit("eori", answers))
      }

      theAssociationSubmitted mustBe AssociateUCRRequest("eori", mucr, ucr)
      verify(repository, never()).removeByEori("eori")
      verify(audit).auditAssociate("eori", mucr, ucr, "Failed")
    }

    "handle missing ucr" in {
      val answers = AssociateUcrAnswers(None, None)
      intercept[Throwable] {
        await(service.submit("eori", answers))
      } mustBe ReturnToStartException

      verifyZeroInteractions(repository)
      verifyZeroInteractions(audit)
    }

    def theAssociationSubmitted: AssociateUCRRequest = {
      val captor: ArgumentCaptor[AssociateUCRRequest] = ArgumentCaptor.forClass(classOf[AssociateUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit Disassociate" should {
    "delegate to connector" when {
      "Disassociate MUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))
        given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some(DisassociateUcr(DisassociateKind.Mucr, None, Some("ucr"))))
        await(service.submit("eori", answers))

        theDisassociationSubmitted mustBe DisassociateDUCRRequest("eori", "ucr")
        verify(repository).removeByEori("eori")
        verify(audit).auditDisassociate("eori", "ucr", "Success")
      }

      "Disassociate DUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))
        given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some(DisassociateUcr(DisassociateKind.Ducr, Some("ucr"), None)))
        await(service.submit("eori", answers))

        theDisassociationSubmitted mustBe DisassociateDUCRRequest("eori", "ucr")
        verify(repository).removeByEori("eori")
        verify(audit).auditDisassociate("eori", "ucr", "Success")
      }
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = DisassociateUcrAnswers(Some(DisassociateUcr(DisassociateKind.Mucr, None, Some("ucr"))))
      intercept[RuntimeException] {
        await(service.submit("eori", answers))
      }

      theDisassociationSubmitted mustBe DisassociateDUCRRequest("eori", "ucr")
      verify(repository, never()).removeByEori("eori")
      verify(audit).auditDisassociate("eori", "ucr", "Failed")
    }

    "handle missing ucr" when {
      "block is empty" in {
        val answers = DisassociateUcrAnswers(None)
        intercept[Throwable] {
          await(service.submit("eori", answers))
        } mustBe ReturnToStartException

        verifyZeroInteractions(repository)
        verifyZeroInteractions(audit)
      }

      "missing fields" when {
        "Disassociate MUCR" in {
          val answers = DisassociateUcrAnswers(Some(DisassociateUcr(DisassociateKind.Mucr, None, None)))
          intercept[Throwable] {
            await(service.submit("eori", answers))
          } mustBe ReturnToStartException

          verifyZeroInteractions(repository)
          verifyZeroInteractions(audit)
        }

        "Disassociate DUCR" in {
          val answers = DisassociateUcrAnswers(Some(DisassociateUcr(DisassociateKind.Ducr, None, None)))
          intercept[Throwable] {
            await(service.submit("eori", answers))
          } mustBe ReturnToStartException

          verifyZeroInteractions(repository)
          verifyZeroInteractions(audit)
        }
      }
    }

    def theDisassociationSubmitted: DisassociateDUCRRequest = {
      val captor: ArgumentCaptor[DisassociateDUCRRequest] = ArgumentCaptor.forClass(classOf[DisassociateDUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit ShutMUCR" should {
    "delegate to connector" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful((): Unit))
      given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

      val answers = ShutMucrAnswers(Some(ShutMucr("mucr")))
      await(service.submit("eori", answers))

      theShutMucrSubmitted mustBe ShutMUCRRequest("eori", "mucr")
      verify(repository).removeByEori("eori")
      verify(audit).auditShutMucr("eori", "mucr", "Success")
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = ShutMucrAnswers(Some(ShutMucr("mucr")))
      intercept[RuntimeException] {
        await(service.submit("eori", answers))
      }

      theShutMucrSubmitted mustBe ShutMUCRRequest("eori", "mucr")
      verify(repository, never()).removeByEori("eori")
      verify(audit).auditShutMucr("eori", "mucr", "Failed")
    }

    def theShutMucrSubmitted: ShutMUCRRequest = {
      val captor: ArgumentCaptor[ShutMUCRRequest] = ArgumentCaptor.forClass(classOf[ShutMUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

}
