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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import play.api.Configuration
import play.api.http.Status

trait MovementsBackendWiremockTestServer extends WiremockTestServer {

  protected val movementsBackendConfiguration: Configuration =
    Configuration.from(Map("microservice.services.customs-declare-exports-movements.port" -> wirePort))

  protected def givenMovementsBackendAcceptsTheConsolidation(): Unit =
    stubFor(
      post("/consolidation")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
        )
    )

  protected def givenTheMovementsBackendAcceptsTheMovement(): Unit =
    stubFor(
      post("/movements")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
        )
    )

  protected def postRequestedForConsolidation(): RequestPatternBuilder = postRequestedFor(urlEqualTo("/consolidation"))
  protected def postRequestedForMovement(): RequestPatternBuilder = postRequestedFor(urlEqualTo("/movements"))

}
