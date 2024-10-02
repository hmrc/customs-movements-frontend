/*
 * Copyright 2024 HM Revenue & Customs
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

import base.{MovementsMetricsStub, UnitSpec}
import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges._
import forms._
import models.ReturnToStartException
import models.cache.{AssociateUcrAnswers, DisassociateUcrAnswers, MovementAnswers, ShutMucrAnswers}
import models.confirmation.SubmissionResult
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.never
import org.mockito.MockitoSugar.{mock, reset, verify, verifyNoMoreInteractions}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import repositories.CacheRepository
import services.audit.{AuditService, AuditType}
import testdata.CommonTestData._
import testdata.MovementsTestData._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends UnitSpec with MovementsMetricsStub with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private val audit = mock[AuditService]
  private val repository = mock[CacheRepository]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val movementBuilder = mock[MovementBuilder]
  private val service = new SubmissionService(repository, connector, audit, movementsMetricsStub, movementBuilder)

  private val conversationId = "conversationId"

  private val mucr = correctUcr_2
  private val ucr = correctUcr

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(audit, connector, repository, movementBuilder)
  }

  override def afterEach(): Unit = {
    reset(audit, connector, repository, movementBuilder)
    super.afterEach()
  }

  "Submit Associate" should {

    "delegate to connector" when {

      "Associate DUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful(conversationId))
        given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

        val answers = AssociateUcrAnswers(None, Some(MucrOptions(MucrOptions.Create, mucr)), Some(AssociateUcr(UcrType.Ducr, ucr)))
        await(service.submit(validEori, answers))

        theAssociationSubmitted mustBe AssociateDUCRRequest(validEori, mucr, ucr)
        verify(repository).removeByEori(validEori)
        verify(audit).auditAssociate(validEori, mucr, ucr, "Success", Some(conversationId))
      }

      "Associate MUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful(conversationId))
        given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

        val answers = AssociateUcrAnswers(None, Some(MucrOptions(MucrOptions.Create, mucr)), Some(AssociateUcr(UcrType.Mucr, ucr)))
        await(service.submit(validEori, answers))

        theAssociationSubmitted mustBe AssociateMUCRRequest(validEori, mucr, ucr)
        verify(repository).removeByEori(validEori)
        verify(audit).auditAssociate(validEori, mucr, ucr, "Success", Some(conversationId))
      }
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = AssociateUcrAnswers(None, Some(MucrOptions(MucrOptions.Create, mucr)), Some(AssociateUcr(UcrType.Ducr, ucr)))
      intercept[RuntimeException] {
        await(service.submit(validEori, answers))
      }

      theAssociationSubmitted mustBe AssociateDUCRRequest(validEori, mucr, ucr)
      verify(repository, never()).removeByEori(validEori)
      verify(audit).auditAssociate(validEori, mucr, ucr, "Failed")
    }

    "handle missing ucr" in {
      val answers = AssociateUcrAnswers(None, None)
      intercept[Throwable] {
        await(service.submit(validEori, answers))
      } mustBe ReturnToStartException

      verifyNoMoreInteractions(repository)
      verifyNoMoreInteractions(audit)
    }

    def theAssociationSubmitted: Consolidation = {
      val captor: ArgumentCaptor[Consolidation] = ArgumentCaptor.forClass(classOf[Consolidation])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit Disassociate" should {

    "delegate to connector" when {

      "Disassociate MUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful(conversationId))
        given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some(DisassociateUcr(UcrType.Mucr, None, Some(ucr))))
        await(service.submit(validEori, answers))

        theDisassociationSubmitted mustBe DisassociateMUCRRequest(validEori, ucr)
        verify(repository).removeByEori(validEori)
        verify(audit).auditDisassociate(validEori, ucr, "Success", Some(conversationId))
      }

      "Disassociate DUCR" in {
        given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful(conversationId))
        given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

        val answers = DisassociateUcrAnswers(Some(DisassociateUcr(UcrType.Ducr, Some(ucr), None)))
        await(service.submit(validEori, answers))

        theDisassociationSubmitted mustBe DisassociateDUCRRequest(validEori, ucr)
        verify(repository).removeByEori(validEori)
        verify(audit).auditDisassociate(validEori, ucr, "Success", Some(conversationId))
      }
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = DisassociateUcrAnswers(Some(DisassociateUcr(UcrType.Mucr, None, Some(ucr))))
      intercept[RuntimeException] {
        await(service.submit(validEori, answers))
      }

      theDisassociationSubmitted mustBe DisassociateMUCRRequest(validEori, ucr)
      verify(repository, never()).removeByEori(validEori)
      verify(audit).auditDisassociate(validEori, ucr, "Failed")
    }

    "handle missing ucr" when {

      "block is empty" in {
        val answers = DisassociateUcrAnswers(None)
        intercept[Throwable] {
          await(service.submit(validEori, answers))
        } mustBe ReturnToStartException

        verifyNoMoreInteractions(repository)
        verifyNoMoreInteractions(audit)
      }

      "missing fields" when {

        "Disassociate MUCR" in {
          val answers = DisassociateUcrAnswers(Some(DisassociateUcr(UcrType.Mucr, None, None)))
          intercept[Throwable] {
            await(service.submit(validEori, answers))
          } mustBe ReturnToStartException

          verifyNoMoreInteractions(repository)
          verifyNoMoreInteractions(audit)
        }

        "Disassociate DUCR" in {
          val answers = DisassociateUcrAnswers(Some(DisassociateUcr(UcrType.Ducr, None, None)))
          intercept[Throwable] {
            await(service.submit(validEori, answers))
          } mustBe ReturnToStartException

          verifyNoMoreInteractions(repository)
          verifyNoMoreInteractions(audit)
        }

        "Disassociate DUCR Part" in {
          val answers = DisassociateUcrAnswers(Some(DisassociateUcr(UcrType.DucrPart, None, None)))
          intercept[Throwable] {
            await(service.submit(validEori, answers))
          } mustBe ReturnToStartException

          verifyNoMoreInteractions(repository)
          verifyNoMoreInteractions(audit)
        }
      }
    }

    def theDisassociationSubmitted: Consolidation = {
      val captor: ArgumentCaptor[Consolidation] = ArgumentCaptor.forClass(classOf[Consolidation])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit ShutMUCR" should {

    "delegate to connector" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.successful(conversationId))
      given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))

      val answers = ShutMucrAnswers(Some(ShutMucr(mucr)))
      await(service.submit(validEori, answers))

      theShutMucrSubmitted mustBe ShutMUCRRequest(validEori, mucr)
      verify(repository).removeByEori(validEori)
      verify(audit).auditShutMucr(validEori, mucr, "Success", Some(conversationId))
    }

    "audit when failed" in {
      given(connector.submit(any[Consolidation]())(any())).willReturn(Future.failed(new RuntimeException("Error")))

      val answers = ShutMucrAnswers(Some(ShutMucr(mucr)))
      intercept[RuntimeException] {
        await(service.submit(validEori, answers))
      }

      theShutMucrSubmitted mustBe ShutMUCRRequest(validEori, mucr)
      verify(repository, never()).removeByEori(validEori)
      verify(audit).auditShutMucr(validEori, mucr, "Failed")
    }

    "handle missing mucr" in {
      val answers = ShutMucrAnswers(None)
      intercept[Throwable] {
        await(service.submit(validEori, answers))
      } mustBe ReturnToStartException

      verifyNoMoreInteractions(repository)
      verifyNoMoreInteractions(audit)
    }

    def theShutMucrSubmitted: ShutMUCRRequest = {
      val captor: ArgumentCaptor[ShutMUCRRequest] = ArgumentCaptor.forClass(classOf[ShutMUCRRequest])
      verify(connector).submit(captor.capture())(any())
      captor.getValue
    }
  }

  "Submit Movement" when {

    "provided with Arrival" when {

      "everything works correctly" should {

        "return same UCR as in the answers" in {
          given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.successful(conversationId))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(validArrivalMovementRequest)

          val answers = validArrivalAnswers

          val submissionResult = await(service.submit(validEori, answers))
          submissionResult mustBe SubmissionResult(conversationId, answers.consignmentReferences.get)
        }

        "call MovementBuilder, AuditService, backend Connector, CacheRepository and AuditService again" in {
          given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.successful(conversationId))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          val arrivalExchange = validArrivalMovementRequest
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(arrivalExchange)

          val answers = validArrivalAnswers

          await(service.submit(validEori, answers))

          val inOrder = Mockito.inOrder(movementBuilder, repository, audit, connector)
          inOrder.verify(movementBuilder).createMovementRequest(meq(validEori), meq(answers))
          inOrder.verify(audit).auditAllPagesUserInput(meq(validEori), meq(answers))(any())
          inOrder.verify(connector).submit(meq(arrivalExchange))(any())
          inOrder.verify(repository).removeByEori(meq(validEori))
          inOrder.verify(audit).auditMovements(meq(arrivalExchange), meq("Success"), meq(AuditType.AuditArrival), meq(Some(conversationId)))(any())
        }
      }

      "MovementBuilder throws ReturnToStartException" should {
        "propagate the exception" in {
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willThrow(ReturnToStartException)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          } mustBe ReturnToStartException
        }

        "not call AuditService, backend Connector and CacheRepository" in {
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willThrow(ReturnToStartException)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          }

          verifyNoMoreInteractions(audit)
          verifyNoMoreInteractions(connector)
          verifyNoMoreInteractions(repository)
        }
      }

      "backend Connector returns Failed Future" should {

        "not call CacheRepository" in {
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.failed(new RuntimeException("Error")))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(validArrivalMovementRequest)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          }

          verifyNoMoreInteractions(repository)
        }

        "call AuditService second time with failed result" in {
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.failed(new RuntimeException("Error")))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          val arrivalExchange = validArrivalMovementRequest
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(arrivalExchange)

          val answers = validArrivalAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          }

          verify(audit).auditMovements(meq(arrivalExchange), meq("Failed"), meq(AuditType.AuditArrival), meq(None))(any())
        }
      }
    }

    "provided with Departure" when {

      "everything works correctly" should {

        "return same UCR as in the answers" in {
          given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.successful(conversationId))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(validDepartureMovementRequest)

          val answers = validDepartureAnswers

          val submissionResult = await(service.submit(validEori, answers))
          submissionResult mustBe SubmissionResult(conversationId, answers.consignmentReferences.get)
        }

        "call MovementBuilder, AuditService, backend Connector, CacheRepository and AuditService again" in {
          given(repository.removeByEori(anyString())).willReturn(Future.successful((): Unit))
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.successful(conversationId))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          val departureExchange = validDepartureMovementRequest
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(departureExchange)

          val answers = validDepartureAnswers

          await(service.submit(validEori, answers))

          val inOrder = Mockito.inOrder(movementBuilder, repository, audit, connector)
          inOrder.verify(movementBuilder).createMovementRequest(meq(validEori), meq(answers))
          inOrder.verify(audit).auditAllPagesUserInput(meq(validEori), meq(answers))(any())
          inOrder.verify(connector).submit(meq(departureExchange))(any())
          inOrder.verify(repository).removeByEori(meq(validEori))
          inOrder
            .verify(audit)
            .auditMovements(meq(departureExchange), meq("Success"), meq(AuditType.AuditDeparture), meq(Some(conversationId)))(any())
        }
      }

      "MovementBuilder throws ReturnToStartException" should {
        "propagate the exception" in {
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willThrow(ReturnToStartException)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          } mustBe ReturnToStartException
        }

        "not call AuditService, backend Connector and CacheRepository" in {
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willThrow(ReturnToStartException)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          }

          verifyNoMoreInteractions(audit)
          verifyNoMoreInteractions(connector)
          verifyNoMoreInteractions(repository)
        }
      }

      "backend Connector returns Failed Future" should {

        "not call CacheRepository" in {
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.failed(new RuntimeException("Error")))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(validDepartureMovementRequest)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          }

          verifyNoMoreInteractions(repository)
        }

        "call AuditService second time with failed result" in {
          given(connector.submit(any[MovementRequest]())(any())).willReturn(Future.failed(new RuntimeException("Error")))
          given(audit.auditAllPagesUserInput(anyString(), any[MovementAnswers])(any())).willReturn(Future.successful(AuditResult.Success))
          given(audit.auditMovements(any[MovementRequest], anyString(), any[AuditType.Audit], any[Option[String]])(any()))
            .willReturn(Future.successful(AuditResult.Success))
          val departureExchange = validDepartureMovementRequest
          given(movementBuilder.createMovementRequest(anyString(), any[MovementAnswers])).willReturn(departureExchange)

          val answers = validDepartureAnswers

          intercept[Throwable] {
            await(service.submit(validEori, answers))
          }

          verify(audit).auditMovements(meq(departureExchange), meq("Failed"), meq(AuditType.AuditDeparture), meq(None))(any())
        }
      }
    }
  }
}
