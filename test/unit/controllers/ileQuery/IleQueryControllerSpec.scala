/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.ileQuery

import java.time.Instant

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.actions.IleQueryAction
import controllers.exception.FeatureDisabledException
import forms.IleQueryForm
import handlers.ErrorHandler
import models.UcrBlock
import models.cache.IleQuery
import models.notifications.queries.IleQueryResponseExchangeData.{SuccessfulResponseExchangeData, UcrNotFoundResponseExchangeData}
import models.notifications.queries._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockIleQueryCache
import testdata.CommonTestData.conversationId
import uk.gov.hmrc.http.HttpResponse
import unit.controllers.ControllerLayerSpec
import unit.repository.MockCache
import views.html._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IleQueryControllerSpec extends ControllerLayerSpec with MockIleQueryCache with MockCache {

  private val errorHandler = mock[ErrorHandler]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]
  private val ileQueryPage = mock[ile_query]
  private val loadingScreenPage = mock[loading_screen]
  private val ileQueryDucrResponsePage = mock[ile_query_ducr_response]
  private val ileQueryMucrResponsePage = mock[ile_query_mucr_response]
  private val consignmentNotFoundPage = mock[consignment_not_found_page]
  private val ileQueryTimeoutPage = mock[ile_query_timeout]

  private def controllerWithIleQuery(ileQueryAction: IleQueryAction): IleQueryController =
    new IleQueryController(
      SuccessfulAuth(),
      ileQueryAction,
      stubMessagesControllerComponents(),
      errorHandler,
      cache,
      ileQueryRepository,
      connector,
      ileQueryPage,
      loadingScreenPage,
      ileQueryDucrResponsePage,
      ileQueryMucrResponsePage,
      consignmentNotFoundPage,
      ileQueryTimeoutPage
    )(global)

  private val controller = controllerWithIleQuery(IleQueryEnabled)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(errorHandler.standardErrorTemplate()(any())).thenReturn(HtmlFormat.empty)
    when(ileQueryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(loadingScreenPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryDucrResponsePage.apply(any[DucrInfo], any[Option[MucrInfo]])(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryMucrResponsePage.apply(any[MucrInfo], any[Option[MucrInfo]], any[Seq[UcrInfo]])(any(), any()))
      .thenReturn(HtmlFormat.empty)
    when(consignmentNotFoundPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryTimeoutPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(
      errorHandler,
      connector,
      ileQueryPage,
      loadingScreenPage,
      ileQueryDucrResponsePage,
      ileQueryMucrResponsePage,
      consignmentNotFoundPage,
      ileQueryTimeoutPage
    )

    super.afterEach()
  }

  private val mucrInfo = MucrInfo(ucr = "mucr")
  private val parentMucrInfo = MucrInfo("parentMucr")
  private val successfulMucrResponseData = SuccessfulResponseExchangeData(queriedMucr = Some(mucrInfo), parentMucr = Some(parentMucrInfo))
  private val successfulMucrResponseExchange =
    IleQueryResponseExchange(Instant.now(), conversationId, "inventoryLinkingQueryResponse", successfulMucrResponseData)

  private val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "DeclarationId")
  private val successfulDucrResponseData = SuccessfulResponseExchangeData(queriedDucr = Some(ducrInfo), parentMucr = Some(parentMucrInfo))
  private val successfulDucrResponseExchange =
    IleQueryResponseExchange(Instant.now(), conversationId, "inventoryLinkingQueryResponse", successfulDucrResponseData)

  private val ucrNotFoundResponseData = UcrNotFoundResponseExchangeData(messageCode = "QUE", actionCode = "1", ucrBlock = Some(UcrBlock("mucr", "M")))
  private val ucrNotFoundResponseExchange =
    IleQueryResponseExchange(Instant.now(), conversationId, "inventoryLinkingControlResponse", ucrNotFoundResponseData)

  "IleQueryController on getConsignmentInformation" should {
    "call IleQueryRepository to find ILE Query cache document" in {

      when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
        .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
      when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
      val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq(successfulMucrResponseExchange))))
      when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

      val request = postRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

      await(controller.getConsignmentInformation("mucr")(request))

      verify(ileQueryRepository) findBySessionIdAndUcr (meq("sessionId"), meq("mucr"))
    }
  }

  "IleQueryController on getConsignmentInformation" when {

    "ileQuery cache is empty for the user" when {

      "provided with correct DUCR" should {

        val correctDucr = "9GB123456-QWERTY7890"

        "call Backend Connector to submit IleQuery, passing constructed IleQuery object" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insert(any[IleQuery])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation(correctDucr)(request))

          val constructedIleQueryCaptor = newIleQueryExchangeCaptor
          verify(connector).submit(constructedIleQueryCaptor.capture())(any())
          val constructedIleQueryExchange = constructedIleQueryCaptor.getValue

          constructedIleQueryExchange.eori mustBe "eori"
          constructedIleQueryExchange.ucrBlock mustBe UcrBlock(ucr = correctDucr, ucrType = "D")
        }

        "call IleQueryRepository to insert cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insert(any[IleQuery])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation(correctDucr)(request))

          val ileQueryCaptor = newIleQueryCaptor
          verify(ileQueryRepository).insert(ileQueryCaptor.capture())(any())
          val actualIleQuery = ileQueryCaptor.getValue

          actualIleQuery.sessionId mustBe "sessionId"
          actualIleQuery.ucr mustBe correctDucr
          actualIleQuery.conversationId mustBe conversationId
        }

        "redirect to the same endpoint" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insert(any[IleQuery])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation(correctDucr)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation(correctDucr).url)
        }
      }

      "provided with correct MUCR" should {

        val correctMucr = "GB/123-QWERTY456"

        "call Backend Connector to submit IleQuery, passing constructed IleQuery object" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insert(any[IleQuery])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation(correctMucr)(request))

          val constructedIleQueryCaptor = newIleQueryExchangeCaptor
          verify(connector).submit(constructedIleQueryCaptor.capture())(any())
          val constructedIleQueryExchange = constructedIleQueryCaptor.getValue

          constructedIleQueryExchange.eori mustBe "eori"
          constructedIleQueryExchange.ucrBlock mustBe UcrBlock(ucr = correctMucr, ucrType = "M")
        }

        "call IleQueryRepository to insert cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insert(any[IleQuery])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation(correctMucr)(request))

          val ileQueryCaptor = newIleQueryCaptor
          verify(ileQueryRepository).insert(ileQueryCaptor.capture())(any())
          val actualIleQuery = ileQueryCaptor.getValue

          actualIleQuery.sessionId mustBe "sessionId"
          actualIleQuery.ucr mustBe correctMucr
          actualIleQuery.conversationId mustBe conversationId
        }

        "redirect to the same endpoint" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insert(any[IleQuery])(any())).thenReturn(Future.successful(dummyWriteResultSuccess))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation(correctMucr)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation(correctMucr).url)
        }
      }

      "provided with semantically incorrect UCR" should {

        val incorrectUCR = "123ABC-789456POIUYT"

        "return BadRequest (400) status" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation(incorrectUCR)(request)

          status(result) mustBe BAD_REQUEST
        }

        "return Find Consignment page, passing form with errors" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation(incorrectUCR)(request))

          val expectedForm = IleQueryForm.form.fillAndValidate(incorrectUCR)
          verify(ileQueryPage).apply(meq(expectedForm))(any(), any())
        }
      }
    }

    "ileQuery cache contains record for queried UCR" should {
      "call Backend Connector to fetch ILE Query Notifications" in {

        when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
        when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
        val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq(successfulMucrResponseExchange))))
        when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

        await(controller.getConsignmentInformation("mucr")(request))

        verify(connector).fetchQueryNotifications(meq(conversationId), meq("eori"))(any())
      }
    }

    "ileQuery cache contains record for queried UCR" when {

      "Backend Connector returns OK (200) response with empty body" should {

        "return Loading page with 'refresh' header" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq.empty[IleQueryResponseExchange])))
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
          verify(loadingScreenPage).apply()(any(), any())
        }
      }

      "Backend Connector returns OK (200) response with Notifications in body" should {

        "call IleQueryRepository to remove cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq(successfulMucrResponseExchange))))
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return ConsignmentNotFound page, if Notification has UcrNotFoundResponseExchangeData" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq(ucrNotFoundResponseExchange))))
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
          verify(consignmentNotFoundPage).apply(meq("mucr"))(any(), any())
        }

        "return DUCR query response page, if Notification has SuccessfulResponseExchangeData with 'queriedDucr'" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq(successfulDucrResponseExchange))))
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("ducr")(request)

          status(result) mustBe OK
          val optMucrInfoCaptor = newOptionalMucrInfoCaptor
          verify(ileQueryDucrResponsePage).apply(meq(ducrInfo), optMucrInfoCaptor.capture())(any(), any())
          optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
          verifyZeroInteractions(ileQueryMucrResponsePage)
        }

        "return MUCR query response page, if Notification has SuccessfulResponseExchangeData with 'queriedMucr'" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(OK, Some(Json.toJson(Seq(successfulMucrResponseExchange))))
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK
          val optMucrInfoCaptor = newOptionalMucrInfoCaptor
          verify(ileQueryMucrResponsePage).apply(meq(mucrInfo), optMucrInfoCaptor.capture(), meq(Seq.empty))(any(), any())
          optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
          verifyZeroInteractions(ileQueryDucrResponsePage)
        }
      }

      "Backend Connector returns a response other than OK (200)" should {

        "call IleQueryRepository to remove cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(responseStatus = SERVICE_UNAVAILABLE)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation("mucr")(request))

          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return InternalServerError response" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(responseStatus = SERVICE_UNAVAILABLE)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }

      "Backend Connector returns FailedDependency (424) response" should {

        "call IleQueryRepository to remove cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(responseStatus = FAILED_DEPENDENCY)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          await(controller.getConsignmentInformation("mucr")(request))

          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return timeout page" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(responseStatus = FAILED_DEPENDENCY)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val request = getRequest.withHeaders(Headers(("X-Session-ID", "sessionId")))

          val result = controller.getConsignmentInformation("mucr")(request)

          status(result) mustBe OK

        }
      }
    }
  }

  "IleQueryController when ileQuery disabled" should {

    val controllerIleQueryDisabled = controllerWithIleQuery(IleQueryDisabled)

    "block access when getting query results" in {

      intercept[RuntimeException] {
        await(controllerIleQueryDisabled.getConsignmentInformation("mucr")(getRequest.withHeaders(Headers(("X-Session-ID", "123456")))))
      } mustBe FeatureDisabledException

    }
  }

  private def newOptionalMucrInfoCaptor: ArgumentCaptor[Option[MucrInfo]] = ArgumentCaptor.forClass(classOf[Option[MucrInfo]])
  private def newIleQueryExchangeCaptor: ArgumentCaptor[IleQueryExchange] = ArgumentCaptor.forClass(classOf[IleQueryExchange])
  private def newIleQueryCaptor: ArgumentCaptor[IleQuery] = ArgumentCaptor.forClass(classOf[IleQuery])

}
