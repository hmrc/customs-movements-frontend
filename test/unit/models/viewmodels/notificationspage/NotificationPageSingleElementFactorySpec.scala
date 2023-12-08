/*
 * Copyright 2023 HM Revenue & Customs
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

package models.viewmodels.notificationspage

import base.UnitSpec
import com.google.inject.Guice
import connectors.exchanges.ActionType.{ConsolidationType, MovementType}
import models.UcrBlock
import models.notifications.Notification
import models.submissions.Submission
import models.viewmodels.notificationspage.converters._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import utils.DateTimeTestModule
import views.MessagesStub

import java.time.Instant

class NotificationPageSingleElementFactorySpec extends UnitSpec with MessagesStub with BeforeAndAfterEach {

  private val testTimestamp: Instant = Instant.parse("2019-10-31T00:00:00Z")

  private implicit val fakeRequest = FakeRequest()

  private val responseConverterProvider = mock[ResponseConverterProvider]
  private val factory = new NotificationPageSingleElementFactory(responseConverterProvider)
  private val injector = Guice.createInjector(new DateTimeTestModule())
  private val unknownResponseConverter = injector.getInstance(classOf[UnknownResponseConverter])

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(responseConverterProvider)
    when(responseConverterProvider.provideResponseConverter(any[Notification]))
      .thenReturn(unknownResponseConverter)
  }

  "NotificationPageSingleElementFactory" should {

    "return NotificationsPageSingleElement with values returned by Messages" when {

      "provided with Arrival Submission" in {

        val input: Submission =
          exampleSubmission(actionType = MovementType.Arrival, requestTimestamp = testTimestamp)

        val expectedTitle = messages("notifications.elem.title.Arrival")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.Arrival", "DUCR"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with Departure Submission" in {

        val input: Submission =
          exampleSubmission(actionType = MovementType.Departure, requestTimestamp = testTimestamp)

        val expectedTitle = messages("notifications.elem.title.Departure")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.Departure", "DUCR"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with DucrAssociation Submission" in {

        val input: Submission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ConsolidationType.DucrAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks =
            Seq(UcrBlock(ucr = correctUcr, ucrType = "M"), UcrBlock(ucr = correctUcr_2, ucrType = "D"), UcrBlock(ucr = correctUcr_3, ucrType = "D"))
        )

        val expectedTitle = messages("notifications.elem.title.DucrAssociation")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.DucrAssociation"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with DucrPartAssociation Submission" in {

        val validDucrPart = "8GB123456789012-123456789-123"

        val input: Submission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ConsolidationType.DucrPartAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(
            UcrBlock(ucr = correctUcr, ucrType = "M"),
            UcrBlock(ucr = correctUcr_2, ucrType = "DP"),
            UcrBlock(ucr = validDucrPart, ucrType = "DP")
          )
        )

        val expectedTitle = messages("notifications.elem.title.DucrPartAssociation")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent =
          Seq(messages("notifications.elem.content.DucrPartAssociation"), validDucrPart, messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with MucrAssociation Submission" in {

        val input: Submission = Submission(
          eori = validEori,
          conversationId = conversationId,
          actionType = ConsolidationType.MucrAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M"), UcrBlock(ucr = correctUcr_2, ucrType = "M"))
        )

        val expectedTitle = messages("notifications.elem.title.MucrAssociation")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.MucrAssociation"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with DucrDisassociation Submission" in {

        val input: Submission =
          exampleSubmission(actionType = ConsolidationType.DucrDisassociation, requestTimestamp = testTimestamp, ucr = correctUcr, ucrType = "D")

        val expectedTitle = messages("notifications.elem.title.DucrDisassociation")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.DucrDisassociation"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with DucrPartDisassociation Submission" in {

        val input: Submission =
          exampleSubmission(actionType = ConsolidationType.DucrPartDisassociation, requestTimestamp = testTimestamp, ucr = correctUcr, ucrType = "DP")

        val expectedTitle = messages("notifications.elem.title.DucrPartDisassociation")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.DucrPartDisassociation"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with MucrDisassociation Submission" in {

        val input: Submission =
          exampleSubmission(actionType = ConsolidationType.MucrDisassociation, requestTimestamp = testTimestamp, ucr = correctUcr, ucrType = "M")

        val expectedTitle = messages("notifications.elem.title.MucrDisassociation")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.MucrDisassociation"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }

      "provided with ShutMucr Submission" in {

        val input: Submission =
          exampleSubmission(actionType = ConsolidationType.ShutMucr, requestTimestamp = testTimestamp, ucr = correctUcr, ucrType = "M")

        val expectedTitle = messages("notifications.elem.title.ShutMucr")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedContent = Seq(messages("notifications.elem.content.ShutMucr"), messages("notifications.elem.content.footer"))

        val result = factory.build(input)

        assertResult(result, expectedTitle, expectedTimestampInfo, expectedContent)
      }
    }
  }

  "NotificationPageSingleElementFactory" when {

    "provided with NotificationFrontendModel" should {

      "call ResponseConverterProvider" in {

        val input = exampleNotificationFrontendModel()

        factory.build(input)

        verify(responseConverterProvider).provideResponseConverter(meq(input))
      }

      "call converter returned by ResponseConverterProvider" in {

        val exampleNotificationPageElement =
          NotificationsPageSingleElement(title = "TITLE", timestampInfo = "TIMESTAMP", content = Html("<test>HTML</test>"))
        val responseConverter = mock[NotificationPageSingleElementConverter]
        when(responseConverter.convert(any[Notification])(any()))
          .thenReturn(exampleNotificationPageElement)

        when(responseConverterProvider.provideResponseConverter(any[Notification]))
          .thenReturn(responseConverter)

        val input = exampleNotificationFrontendModel()

        factory.build(input)

        verify(responseConverter).convert(meq(input))(any[Messages])
      }

      "return NotificationsPageSingleElement returned by converter" in {

        val exampleNotificationPageElement =
          NotificationsPageSingleElement(title = "TITLE", timestampInfo = "TIMESTAMP", content = Html("<test>HTML</test>"))
        val responseConverter = mock[NotificationPageSingleElementConverter]
        when(responseConverter.convert(any[Notification])(any()))
          .thenReturn(exampleNotificationPageElement)

        when(responseConverterProvider.provideResponseConverter(any[Notification]))
          .thenReturn(responseConverter)

        val input = exampleNotificationFrontendModel()

        val result = factory.build(input)

        result mustBe exampleNotificationPageElement
      }
    }
  }

  private def assertResult(
    actual: NotificationsPageSingleElement,
    expectedTitle: String,
    expectedTimestampInfo: String,
    expectedContentElements: Seq[String]
  ): Unit = {
    actual.title mustBe expectedTitle
    actual.timestampInfo mustBe expectedTimestampInfo

    val contentAsString = actual.content.toString
    expectedContentElements.foreach { contentElement =>
      contentAsString must include(contentElement)
    }
  }

}
