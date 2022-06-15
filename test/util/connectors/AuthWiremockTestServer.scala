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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.Helpers.{OK, UNAUTHORIZED}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}

trait AuthWiremockTestServer extends WiremockTestServer {

  protected val authConfiguration: Configuration = Configuration.from(Map("microservice.services.auth.port" -> wirePort))

  protected def givenAuthSuccess(eori: String): Unit = givenAuthSuccess(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", eori)), ""))

  protected def givenAuthSuccess(roles: Enrolment*): Unit = {
    val response = Json.obj("allEnrolments" -> roles.map { role =>
      Json.obj(
        "key" -> role.key,
        "identifiers" -> role.identifiers.map { id =>
          Json.obj("key" -> id.key, "value" -> id.value)
        },
        "state" -> "state"
      )
    })

    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(response.toString())
        )
    )
  }

  protected def givenAuthFailed(): Unit =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(UNAUTHORIZED)
            .withHeader("WWW-Authenticate", "MDTP detail=\"InvalidBearerToken\"")
        )
    )

}
