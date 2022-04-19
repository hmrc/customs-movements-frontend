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

package models.viewmodels.notificationspage.converters

import java.time.{Instant, LocalDate, ZoneOffset}
import base.BaseSpec
import com.google.inject.{AbstractModule, Guice}
import models.notifications.{Entry, EntryStatus, ResponseType}
import models.viewmodels.decoder.{CRCCode, Decoder}
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import models.viewmodels.notificationspage.converters.ERSResponseConverterSpec.{roeKeyFromDecoder, soeKeyFromDecoder}
import models.UcrBlock
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.correctUcr
import testdata.NotificationTestData.exampleNotificationFrontendModel
import utils.DateTimeTestModule

class MovementResponseConverterSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  private val testTimestamp: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  private val crcCodeKeyFromDecoder = CRCCode.Success

  private implicit val messages: Messages = stubMessages()

  private val decoder: Decoder = mock[Decoder]

  private val injector = Guice.createInjector(new DateTimeTestModule(), new AbstractModule {
    override def configure(): Unit = bind(classOf[Decoder]).toInstance(decoder)
  })

  private val converter = injector.getInstance(classOf[MovementResponseConverter])

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(decoder)
    when(decoder.crc(any[String])).thenReturn(Some(crcCodeKeyFromDecoder))
    when(decoder.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoder.ducrSoe(any[String])).thenReturn(Some(soeKeyFromDecoder))
  }

  "MovementResponseConverter on build" when {

    "provided with MovementResponse NotificationFrontendModel" should {

      "call Decoder" in {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          crcCode = Some(crcCodeKeyFromDecoder.code),
          entries = Seq(
            Entry(
              ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
              entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder), soe = Some(soeKeyFromDecoder.code)))
            )
          )
        )

        converter.convert(input)

        verify(decoder).crc(meq(crcCodeKeyFromDecoder.code))
      }

      "return NotificationsPageSingleElement with values returned by Messages" in {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          entries = Seq(
            Entry(
              ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
              entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder), soe = Some(soeKeyFromDecoder.code)))
            )
          ),
          timestampReceived = testTimestamp,
          crcCode = Some(crcCodeKeyFromDecoder.code)
        )
        val expectedTitle = messages("notifications.elem.title.inventoryLinkingMovementResponse")
        val expectedTimestampInfo = "31 October 2019 at 12:00am"
        val expectedCrcContent = s"${messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")} ${crcCodeKeyFromDecoder.messageKey}"
        val expectedSoeContent = s"${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${soeKeyFromDecoder.code}"
        val expectedRoeContent = s"${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${roeKeyFromDecoder.code}"

        val result = converter.convert(input)

        result.title mustBe expectedTitle
        result.timestampInfo mustBe expectedTimestampInfo
        result.content.toString must include(expectedCrcContent)
        result.content.toString must include(expectedSoeContent)
        result.content.toString must include(expectedRoeContent)
      }
    }

    "provided with MovementResponse with missing codes" should {

      "not call Decoder" in {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse)

        converter.convert(input)

        verify(decoder, times(0)).crc(any[String])
      }

      "return NotificationsPageSingleElement without content for missing codes" in {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse, timestampReceived = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = "31 October 2019 at 12:00am",
          content = HtmlFormat.empty
        )

        converter.convert(input) mustBe expectedResult
      }
    }

    "provided with MovementResponse with unknown codes" should {

      "call Decoder" in {

        val crcCode = "123456"
        val input =
          exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse, crcCode = Some(crcCode))

        converter.convert(input)

        verify(decoder).crc(meq(crcCode))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in {

        val crcCode = "123456"
        when(decoder.crc(meq(crcCode))).thenReturn(None)

        val input =
          exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse, timestampReceived = testTimestamp, crcCode = Some(crcCode))
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = "31 October 2019 at 12:00am",
          content = HtmlFormat.empty
        )

        converter.convert(input) mustBe expectedResult
      }
    }
  }
}
