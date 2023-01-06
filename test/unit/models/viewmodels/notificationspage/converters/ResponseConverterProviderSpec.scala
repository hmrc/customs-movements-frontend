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

import base.UnitSpec
import models.notifications.ResponseType._
import models.viewmodels.decoder.ActionCode
import models.viewmodels.notificationspage.MovementTotalsResponseType.{EMR, ERS}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ResponseConverterProviderSpec extends UnitSpec with BeforeAndAfterEach {

  implicit val messages: Messages = stubMessages()

  private val controlResponseAcknowledgedConverter: ControlResponseAcknowledgedConverter =
    mock[ControlResponseAcknowledgedConverter]
  private val controlResponseBlockedConverter: ControlResponseBlockedConverter = mock[ControlResponseBlockedConverter]
  private val controlResponseRejectedConverter: ControlResponseRejectedConverter = mock[ControlResponseRejectedConverter]
  private val ersResponseConverter: ERSResponseConverter = mock[ERSResponseConverter]
  private val emrResponseConverter: EMRResponseConverter = mock[EMRResponseConverter]
  private val movementResponseConverter: MovementResponseConverter = mock[MovementResponseConverter]
  private val unknownResponseConverter: UnknownResponseConverter = mock[UnknownResponseConverter]

  private val provider = new ResponseConverterProvider(
    controlResponseAcknowledgedConverter,
    controlResponseBlockedConverter,
    controlResponseRejectedConverter,
    ersResponseConverter,
    emrResponseConverter,
    movementResponseConverter,
    unknownResponseConverter
  )

  "ResponseConverterProvider" should {

    "return correct Response Converter" when {

      "provided with MovementResponse" in {

        val input = exampleNotificationFrontendModel(responseType = MovementResponse)

        val converter = provider.provideResponseConverter(input)

        converter mustBe movementResponseConverter
      }

      "provided with ERS MovementTotalsResponse" in {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = ERS.code)

        val converter = provider.provideResponseConverter(input)

        converter mustBe ersResponseConverter
      }

      "provided with EMR MovementTotalsResponse" in {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = EMR.code)

        val converter = provider.provideResponseConverter(input)

        converter mustBe emrResponseConverter
      }

      "provided with ControlResponse" which {

        "is Acknowledged" in {

          val input = exampleNotificationFrontendModel(responseType = ControlResponse, actionCode = Some(ActionCode.AcknowledgedAndProcessed.code))

          val converter = provider.provideResponseConverter(input)

          converter mustBe controlResponseAcknowledgedConverter
        }

        "is Blocked" in {

          val input =
            exampleNotificationFrontendModel(responseType = ControlResponse, actionCode = Some(ActionCode.PartiallyAcknowledgedAndProcessed.code))

          val converter = provider.provideResponseConverter(input)

          converter mustBe controlResponseBlockedConverter
        }

        "is Rejected" in {

          val input = exampleNotificationFrontendModel(responseType = ControlResponse, actionCode = Some(ActionCode.Rejected.code))

          val converter = provider.provideResponseConverter(input)

          converter mustBe controlResponseRejectedConverter
        }
      }
    }

    "return Unknown Response Converter" when {

      "provided with unknown response" in {

        val input = exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = "UNKNOWN")

        val converter = provider.provideResponseConverter(input)

        converter mustBe unknownResponseConverter
      }

      "provided with ControlResponse with unknown ActionCode" in {

        val input = exampleNotificationFrontendModel(responseType = ControlResponse, actionCode = Some("UNKNOWN"))

        val converter = provider.provideResponseConverter(input)

        converter mustBe unknownResponseConverter
      }
    }
  }

}
