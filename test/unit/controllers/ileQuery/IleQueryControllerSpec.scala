/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.ControllerLayerSpec
import controllers.actions.IleQueryAction
import controllers.exception.InvalidFeatureStateException
import controllers.ileQuery.routes.IleQueryController
import forms.IleQueryForm
import forms.UcrType.Mucr
import handlers.ErrorHandler
import models.UcrBlock
import models.cache.{Cache, IleQuery}
import models.notifications.queries.IleQueryResponseExchangeData.{SuccessfulResponseExchangeData, UcrNotFoundResponseExchangeData}
import models.notifications.queries._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Headers, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.{MockCache, MockIleQueryCache}
import testdata.CommonTestData.conversationId
import testdata.MovementsTestData.exampleIleQuery
import uk.gov.hmrc.http.HttpResponse
import views.html._

import java.time.Instant
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
      cacheRepository,
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

  private val request: Request[AnyContent] = getRequest().withHeaders(Headers(("X-Session-ID", "sessionId")))

  private val mucrInfo = MucrInfo(ucr = "mucr")
  private val parentMucrInfo = MucrInfo("parentMucr")
  private val successfulMucrResponseData = SuccessfulResponseExchangeData(queriedMucr = Some(mucrInfo), parentMucr = Some(parentMucrInfo))
  private val successfulMucrResponseExchange =
    IleQueryResponseExchange(Instant.now(), conversationId, "inventoryLinkingQueryResponse", Some(successfulMucrResponseData))

  private val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "DeclarationId")
  private val successfulDucrResponseData = SuccessfulResponseExchangeData(queriedDucr = Some(ducrInfo), parentMucr = Some(parentMucrInfo))
  private val successfulDucrResponseExchange =
    IleQueryResponseExchange(Instant.now(), conversationId, "inventoryLinkingQueryResponse", Some(successfulDucrResponseData))

  private val ucrNotFoundResponseData =
    UcrNotFoundResponseExchangeData(messageCode = "QUE", actionCode = "1", ucrBlock = Some(UcrBlock(ucr = "mucr", ucrType = Mucr)))
  private val ucrNotFoundResponseExchange =
    IleQueryResponseExchange(Instant.now(), conversationId, "inventoryLinkingControlResponse", Some(ucrNotFoundResponseData))

  "IleQueryController on getConsignmentData" should {
    "call IleQueryRepository to find ILE Query cache document" in {

      when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
        .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
      when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
      val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
      when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

      await(controller.getConsignmentData("mucr")(request))

      verify(ileQueryRepository) findBySessionIdAndUcr (meq("sessionId"), meq("mucr"))
    }
  }

  "IleQueryController on getConsignmentData" when {

    "ileQuery cache is empty for the user" when {

      "provided with correct DUCR" should {

        val correctDucr = "9GB123456789012-QWERTY7890"

        "call Backend Connector to submit IleQuery, passing constructed IleQuery object" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          await(controller.getConsignmentData(correctDucr)(request))

          val constructedIleQueryCaptor = newIleQueryExchangeCaptor
          verify(connector).submit(constructedIleQueryCaptor.capture())(any())
          val constructedIleQueryExchange = constructedIleQueryCaptor.getValue

          constructedIleQueryExchange.eori mustBe "eori"
          constructedIleQueryExchange.ucrBlock mustBe UcrBlock(ucr = correctDucr, ucrType = "D")
        }

        "call IleQueryRepository to insert cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          await(controller.getConsignmentData(correctDucr)(request))

          val ileQueryCaptor = newIleQueryCaptor
          verify(ileQueryRepository).insertOne(ileQueryCaptor.capture())
          val actualIleQuery = ileQueryCaptor.getValue

          actualIleQuery.sessionId mustBe "sessionId"
          actualIleQuery.ucr mustBe correctDucr
          actualIleQuery.conversationId mustBe conversationId
        }

        "redirect to the same endpoint" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val result = controller.getConsignmentData(correctDucr)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(IleQueryController.getConsignmentData(correctDucr).url)
        }
      }

      "provided with correct MUCR" should {

        val correctMucr = "GB/123-QWERTY456"

        "call Backend Connector to submit IleQuery, passing constructed IleQuery object" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          await(controller.getConsignmentData(correctMucr)(request))

          val constructedIleQueryCaptor = newIleQueryExchangeCaptor
          verify(connector).submit(constructedIleQueryCaptor.capture())(any())
          val constructedIleQueryExchange = constructedIleQueryCaptor.getValue

          constructedIleQueryExchange.eori mustBe "eori"
          constructedIleQueryExchange.ucrBlock mustBe UcrBlock(ucr = correctMucr, ucrType = "M")
        }

        "call IleQueryRepository to insert cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          await(controller.getConsignmentData(correctMucr)(request))

          val ileQueryCaptor = newIleQueryCaptor
          verify(ileQueryRepository).insertOne(ileQueryCaptor.capture())
          val actualIleQuery = ileQueryCaptor.getValue

          actualIleQuery.sessionId mustBe "sessionId"
          actualIleQuery.ucr mustBe correctMucr
          actualIleQuery.conversationId mustBe conversationId
        }

        "redirect to the same endpoint" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))
          when(ileQueryRepository.insertOne(any[IleQuery])).thenReturn(Future.successful(Right(exampleIleQuery())))
          when(connector.submit(any[IleQueryExchange])(any())).thenReturn(Future.successful(conversationId))

          val result = controller.getConsignmentData(correctMucr)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(IleQueryController.getConsignmentData(correctMucr).url)
        }
      }

      "provided with semantically incorrect UCR" should {

        val incorrectUCR = "123ABC-789456POIUYT"

        "return BadRequest (400) status" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))

          val result = controller.getConsignmentData(incorrectUCR)(request)

          status(result) mustBe BAD_REQUEST
        }

        "return Find Consignment page, passing form with errors" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString())).thenReturn(Future.successful(None))

          await(controller.getConsignmentData(incorrectUCR)(request))

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
        val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
        when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

        await(controller.getConsignmentData("mucr")(request))

        verify(connector).fetchQueryNotifications(meq(conversationId), meq("eori"))(any())
      }
    }

    "ileQuery cache contains record for queried UCR" when {

      "Backend Connector returns OK (200) response with empty body" should {

        "return Loading page with 'refresh' header" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq.empty[IleQueryResponseExchange]), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("mucr")(request)

          status(result) mustBe OK
          verify(loadingScreenPage).apply()(any(), any())
        }
      }

      "Backend Connector returns OK (200) response with Notifications in body" should {

        "call IleQueryRepository to remove cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("mucr")(request)

          status(result) mustBe OK
          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return ConsignmentNotFound page, if Notification has UcrNotFoundResponseExchangeData" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(ucrNotFoundResponseExchange)), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("mucr")(request)

          status(result) mustBe OK
          verify(consignmentNotFoundPage).apply(meq("mucr"))(any(), any())
        }

        "return DUCR query response page, if Notification has SuccessfulResponseExchangeData with 'queriedDucr'" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulDucrResponseExchange)), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("ducr")(request)

          status(result) mustBe OK
          val optMucrInfoCaptor = newOptionalMucrInfoCaptor
          verify(ileQueryDucrResponsePage).apply(meq(ducrInfo), optMucrInfoCaptor.capture())(any(), any())
          optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
          verifyNoMoreInteractions(ileQueryMucrResponsePage)
        }

        "return MUCR query response page, if Notification has SuccessfulResponseExchangeData with 'queriedMucr'" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("mucr")(request)

          status(result) mustBe OK
          val optMucrInfoCaptor = newOptionalMucrInfoCaptor
          verify(ileQueryMucrResponsePage).apply(meq(mucrInfo), optMucrInfoCaptor.capture(), meq(Seq.empty))(any(), any())
          optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
          verifyNoMoreInteractions(ileQueryDucrResponsePage)
        }
      }

      "Backend Connector returns OK (200) response with Notifications in body" when {

        "Notification has UcrNotFoundResponseExchangeData" should {
          "return ConsignmentNotFound page" in {

            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(ucrNotFoundResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            val result = controller.getConsignmentData("mucr")(request)

            status(result) mustBe OK
            verify(consignmentNotFoundPage).apply(meq("mucr"))(any(), any())
          }
        }

        "Notification has SuccessfulResponseExchangeData with 'queriedDucr'" should {

          "call CacheRepository to upsert queried DUCR" in {

            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulDucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            await(controller.getConsignmentData("ducr")(request))

            val cacheCaptor = newCacheCaptor
            verify(cacheRepository).upsert(cacheCaptor.capture())
            val cacheUpserted = cacheCaptor.getValue

            cacheUpserted.eori mustBe "eori"
            cacheUpserted.ucrBlock.get mustBe UcrBlock(ucr = "ducr", ucrType = "D")
            cacheUpserted.answers mustBe None
          }

          "return DUCR query response page" in {

            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "ducr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulDucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            val result = controller.getConsignmentData("ducr")(request)

            status(result) mustBe OK
            val optMucrInfoCaptor = newOptionalMucrInfoCaptor
            verify(ileQueryDucrResponsePage).apply(meq(ducrInfo), optMucrInfoCaptor.capture())(any(), any())
            optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
            verifyNoMoreInteractions(ileQueryMucrResponsePage)
          }
        }

        "Notification has SuccessfulResponseExchangeData with 'queriedMucr'" should {

          "call CacheRepository to upsert queried MUCR" in {

            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            await(controller.getConsignmentData("mucr")(request))

            val cacheCaptor = newCacheCaptor
            verify(cacheRepository).upsert(cacheCaptor.capture())
            val cacheUpserted = cacheCaptor.getValue

            cacheUpserted.eori mustBe "eori"
            cacheUpserted.ucrBlock.get mustBe UcrBlock(ucr = "mucr", ucrType = "M")
            cacheUpserted.answers mustBe None
          }

          "return MUCR query response page" in {

            when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
              .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
            when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
            val backendResponse = HttpResponse(status = OK, json = Json.toJson(Seq(successfulMucrResponseExchange)), headers = Map.empty)
            when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

            val result = controller.getConsignmentData("mucr")(request)

            status(result) mustBe OK
            val optMucrInfoCaptor = newOptionalMucrInfoCaptor
            verify(ileQueryMucrResponsePage).apply(meq(mucrInfo), optMucrInfoCaptor.capture(), meq(Seq.empty))(any(), any())
            optMucrInfoCaptor.getValue mustBe Some(parentMucrInfo)
            verifyNoMoreInteractions(ileQueryDucrResponsePage)
          }
        }
      }

      "Backend Connector returns a response other than OK (200)" should {

        "call IleQueryRepository to remove cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = SERVICE_UNAVAILABLE, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          await(controller.getConsignmentData("mucr")(request))

          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return InternalServerError response" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = SERVICE_UNAVAILABLE, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("mucr")(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }

      "Backend Connector returns FailedDependency (424) response" should {

        "call IleQueryRepository to remove cache document" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = FAILED_DEPENDENCY, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          await(controller.getConsignmentData("mucr")(request))

          verify(ileQueryRepository).removeByConversationId(meq(conversationId))
        }

        "return timeout page" in {

          when(ileQueryRepository.findBySessionIdAndUcr(anyString(), anyString()))
            .thenReturn(Future.successful(Some(IleQuery("sessionId", "mucr", conversationId))))
          when(ileQueryRepository.removeByConversationId(anyString())).thenReturn(Future.successful((): Unit))
          val backendResponse = HttpResponse(status = FAILED_DEPENDENCY, body = "")
          when(connector.fetchQueryNotifications(anyString(), anyString())(any())).thenReturn(Future.successful(backendResponse))

          val result = controller.getConsignmentData("mucr")(request)

          status(result) mustBe OK

        }
      }
    }
  }

  "IleQueryController when ileQuery disabled" should {

    val controllerIleQueryDisabled = controllerWithIleQuery(IleQueryDisabled)

    "block access when getting query results" in {

      intercept[RuntimeException] {
        await(controllerIleQueryDisabled.getConsignmentData("mucr")(getRequest.withHeaders(Headers(("X-Session-ID", "123456")))))
      } mustBe InvalidFeatureStateException
    }
  }

  private def newOptionalMucrInfoCaptor: ArgumentCaptor[Option[MucrInfo]] = ArgumentCaptor.forClass(classOf[Option[MucrInfo]])
  private def newIleQueryExchangeCaptor: ArgumentCaptor[IleQueryExchange] = ArgumentCaptor.forClass(classOf[IleQueryExchange])
  private def newIleQueryCaptor: ArgumentCaptor[IleQuery] = ArgumentCaptor.forClass(classOf[IleQuery])
  private def newCacheCaptor: ArgumentCaptor[Cache] = ArgumentCaptor.forClass(classOf[Cache])
}
