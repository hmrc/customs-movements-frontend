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
import handlers.ErrorHandler
import models.cache.IleQuery
import models.notifications.queries.IleQueryResponseExchangeData.{SuccessfulResponseExchangeData, UcrNotFoundResponseExchangeData}
import models.notifications.queries._
import models.{FeatureDisabledException, UcrBlock}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{never, reset, verify, when}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockIleQueryCache
import testdata.CommonTestData.correctUcr
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
      consignmentNotFoundPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(errorHandler.standardErrorTemplate()(any())).thenReturn(HtmlFormat.empty)
    when(ileQueryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(loadingScreenPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryDucrResponsePage.apply(any[DucrInfo], any[Option[MucrInfo]])(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryMucrResponsePage.apply(any[MucrInfo], any[Option[MucrInfo]], any[Seq[UcrInfo]])(any(), any()))
      .thenReturn(HtmlFormat.empty)
    when(consignmentNotFoundPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(errorHandler, connector, ileQueryPage, loadingScreenPage, ileQueryDucrResponsePage, ileQueryMucrResponsePage, consignmentNotFoundPage)

    super.afterEach()
  }

  "Ile Query Controller when ileQuery enabled" should {

    val controller = controllerWithIleQuery(IleQueryEnabled)

    "return 200 (OK)" when {

      "display query form method is invoked" in {

        val result = controller.displayQueryForm()(getRequest)

        status(result) mustBe OK
      }

      "submit mucr query method is invoked and notifications are available" in {

        val mucrInfo = MucrInfo("mucr")
        val parentMucrInfo = Some(MucrInfo("parentMucr"))
        val responseData = SuccessfulResponseExchangeData(queriedMucr = Some(mucrInfo), parentMucr = parentMucrInfo)
        val responseExchange = Seq(IleQueryResponseExchange(Instant.now(), "convId", "inventoryLinkingQueryResponse", responseData))
        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(responseExchange)))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("mucr")(request)

        status(result) mustBe OK
        verify(ileQueryMucrResponsePage).apply(meq(mucrInfo), meq(parentMucrInfo), meq(Seq.empty))(any(), any())

        theCacheUpserted.queryUcr mustBe Some(UcrBlock("mucr", "M"))
      }

      "submit mucr query method is invoked and the mucr is not found " in {

        val responseData = UcrNotFoundResponseExchangeData(messageCode = "", actionCode = "", ucrBlock = Some(UcrBlock("mucr", "M")))
        val responseExchange = Seq(IleQueryResponseExchange(Instant.now(), "convId", "inventoryLinkingQueryResponse", responseData))
        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(responseExchange)))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("mucr")(request)

        status(result) mustBe OK
        verify(consignmentNotFoundPage).apply(any())(any(), any())
      }

      "submit ducr query method is invoked and notifications are available" in {

        val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "decId")
        val parentMucrInfo = Some(MucrInfo("parentMucr"))
        val responseData = SuccessfulResponseExchangeData(queriedDucr = Some(ducrInfo), parentMucr = parentMucrInfo)
        val responseExchange = Seq(IleQueryResponseExchange(Instant.now(), "convId", "inventoryLinkingQueryResponse", responseData))
        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(responseExchange)))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ducr")(request)

        status(result) mustBe OK
        verify(ileQueryDucrResponsePage).apply(meq(ducrInfo), meq(parentMucrInfo))(any(), any())

        theCacheUpserted.queryUcr mustBe Some(UcrBlock("ducr", "D"))
      }

      "submit ducr query method is invoked and the ducr is not found " in {

        val responseData = UcrNotFoundResponseExchangeData(messageCode = "", actionCode = "", ucrBlock = Some(UcrBlock("ducr", "D")))
        val responseExchange = Seq(IleQueryResponseExchange(Instant.now(), "convId", "inventoryLinkingQueryResponse", responseData))
        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(responseExchange)))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ducr")(request)

        status(result) mustBe OK
        verify(consignmentNotFoundPage).apply(any())(any(), any())
      }

      "submit query method is invoked and neither mucr or ducr info available" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(Seq.empty[IleQueryResponseExchange])))))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = postRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("mucr")(request)

        status(result) mustBe OK
        verify(loadingScreenPage).apply()(any(), any())

        verify(cache, never).upsert(any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "query form is incorrect" in {

        val incorrectForm = JsString("1234")

        val result = controller.submitQueryForm()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }

      "ucr is incorrect during submitting ile query" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(None))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ucr")(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {

      "there is a timeout during waiting for notifications" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(GATEWAY_TIMEOUT)))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ucr")(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "connector returned different status than OK" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(Some(IleQuery("sessionId", "ucr", "convId"))))
        when(connector.fetchQueryNotifications(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
        when(ileQueryRepository.removeByConversationId(any()))
          .thenReturn(Future.successful((): Unit))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery("ucr")(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 303 (SEE_OTHER)" when {

      "correct form has been submitted" in {

        val correctForm = Json.obj(("ucr", JsString(correctUcr)))

        val result = controller.submitQueryForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "correct ucr has been submitted" in {

        when(ileQueryRepository.findBySessionIdAndUcr(any(), any()))
          .thenReturn(Future.successful(None))
        when(connector.submit(any[IleQueryExchange])(any()))
          .thenReturn(Future.successful("convId"))
        when(ileQueryRepository.insert(any())(any()))
          .thenReturn(Future.successful(dummyWriteResultSuccess))

        val request = getRequest.withHeaders(Headers(("X-Session-ID", "123456")))

        val result = controller.submitQuery(correctUcr)(request)

        status(result) mustBe SEE_OTHER
      }
    }
  }

  "Ile Query Controller when ileQuery disabled" should {

    val controller = controllerWithIleQuery(IleQueryDisabled)

    "block access to query form" in {

      intercept[RuntimeException] {
        await(controller.displayQueryForm()(getRequest))
      } mustBe FeatureDisabledException

    }

    "block access when posting query form" in {

      val correctForm = Json.obj(("ucr", JsString(correctUcr)))

      intercept[RuntimeException] {
        await(controller.submitQueryForm()(postRequest(correctForm)))
      }
    }

    "block access when getting query results" in {

      intercept[RuntimeException] {
        await(controller.submitQuery("mucr")(getRequest.withHeaders(Headers(("X-Session-ID", "123456")))))
      } mustBe FeatureDisabledException

    }
  }
}
