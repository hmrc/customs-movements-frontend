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

package models.viewmodels.notificationspage.converters

import base.BaseSpec
import com.google.inject.Guice
import models.notifications.ResponseType.MovementTotalsResponse
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import testdata.NotificationTestData.{exampleNotificationFrontendModel, testTimestamp}
import utils.DateTimeTestModule

class UnknownResponseConverterSpec extends BaseSpec {

  private implicit val messages: Messages = stubMessages()

  private val injector = Guice.createInjector(new DateTimeTestModule())

  private val converter = injector.getInstance(classOf[UnknownResponseConverter])

  "UnknownResponseConverter on convert" should {

    "return generic NotificationsPageSingleElement" in {

      val input = exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = "UNKNOWN", timestampReceived = testTimestamp)

      val result = converter.convert(input)

      result.title mustBe messages("notifications.elem.title.unknown")
      result.timestampInfo mustBe "23 Oct 2019 at 12:34"
      result.content mustBe HtmlFormat.empty
    }
  }

}
