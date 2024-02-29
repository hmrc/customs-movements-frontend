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

package models.viewmodels.notificationspage.converters

import base.{OverridableInjector, UnitSpec}
import connectors.exchanges.ActionType.MovementType
import models.notifications.ResponseType
import models.submissions.Submission
import models.viewmodels.decoder.ActionCode
import modules.DateTimeModule
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData
import testdata.NotificationTestData.{exampleNotificationFrontendModel, testTimestamp}
import utils.DateTimeTestModule
import views.MessagesStub

class ControlResponseAcknowledgedConverterSpec extends UnitSpec with MessagesStub {

  import ControlResponseAcknowledgedConverterSpec._

  private implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  private val injector = new OverridableInjector(bind[DateTimeModule].toInstance(new DateTimeTestModule))
  private val converter = injector.instanceOf[ControlResponseAcknowledgedConverter]
  private val submission: Submission =
    exampleSubmission(actionType = MovementType.Arrival, requestTimestamp = testTimestamp)

  "ControlResponseAcknowledgedConverter on convert" should {

    "return NotificationsPageSingleElement with correct title" in {
      val input = ConverterData(AcknowledgedControlResponse, Some(submission))
      val expectedTitle = messages("notifications.elem.title.Arrival.inventoryLinkingControlResponse.AcknowledgedAndProcessed")

      val result = converter.convert(input)

      result.title mustBe expectedTitle
    }

    "return NotificationsPageSingleElement with correct timestampInfo" in {
      val input = ConverterData(AcknowledgedControlResponse, Some(submission))
      val expectedTimestampInfo = "23 October 2019 at 12:34pm"

      val result = converter.convert(input)
      result.timestampInfo mustBe expectedTimestampInfo
    }

    "return NotificationsPageSingleElement with correct content" in {
      val input = ConverterData(AcknowledgedControlResponse, Some(submission))
      val expectedContent = messages("notifications.elem.content.Arrival.inventoryLinkingControlResponse.AcknowledgedAndProcessed")

      val result = converter.convert(input)
      result.content.toString must include(expectedContent)
    }
  }
}

object ControlResponseAcknowledgedConverterSpec {

  val AcknowledgedControlResponse = exampleNotificationFrontendModel(
    responseType = ResponseType.ControlResponse,
    timestampReceived = NotificationTestData.testTimestamp,
    actionCode = Some(ActionCode.AcknowledgedAndProcessed.code)
  )
}
