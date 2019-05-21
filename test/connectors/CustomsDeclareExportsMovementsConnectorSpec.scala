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

package connectors

import base.ExportsTestData.cacheMapData
import base.TestHelper._
import base.{MockHttpClient, MovementBaseSpec, TestHelper}
import config.AppConfig
import forms.Choice.AllowedChoiceValues
import forms.Choice.AllowedChoiceValues.Arrival
import forms.{Choice, Movement}
import metrics.MovementsMetrics
import models._
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.Json
import play.api.mvc.Codec
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.wco.dec.MetaData

class CustomsDeclareExportsMovementsConnectorSpec extends MovementBaseSpec {
  import CustomsDeclareExportsMovementsConnectorSpec._

  val appConfig = mock[AppConfig]

  "Customs Exports Movements Connector" should {

    "submit Movement Declaration to backend" in {
      val http = new MockHttpClient(
        expectedMovementsUrl(appConfig.saveMovementSubmission),
        data.toXml,
        expectedHeaders,
        false,
        HttpResponse(OK, Some(Json.toJson("success")))
      )
      val client = new CustomsDeclareExportsMovementsConnector(appConfig, http)
      val response = client.submitMovementDeclaration(data.ucrBlock.ucr,data.messageCode,data.toXml)(hc, ec)

      response.futureValue.status must be(OK)
    }

  }

  private def expectedMovementsUrl(endpointUrl: String): String =
    s"${appConfig.customsDeclareExportsMovements}$endpointUrl"
}

object CustomsDeclareExportsMovementsConnectorSpec {
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(createRandomAlphanumericString(255))))
  val metadata = MetaData()

  val conversationId: String = TestHelper.createRandomAlphanumericString(10)
  val eori: String = TestHelper.createRandomAlphanumericString(15)



  val data = Movement.createMovementRequest(CacheMap(Arrival, cacheMapData(Arrival)), "eori1",Choice(Arrival))
  val expectedHeaders: Seq[(String, String)] = Seq(
    (HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)),
    (HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8)),
    ("X-UCR", data.ucrBlock.ucr),
    ("X-MOVEMENT-TYPE", data.messageCode)
  )
}
