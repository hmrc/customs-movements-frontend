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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import play.api.http.Status

trait AuditWiremockTestServer extends WiremockTestServer with BeforeAndAfterEach {

  protected val auditConfiguration: Configuration =
    Configuration.from(Map("auditing.consumer.baseUri.port" -> wirePort, "auditing.enabled" -> "true"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    stubFor(
      post("/write/audit/merged")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
        )
    )
    stubFor(
      post("/write/audit")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
        )
    )
  }

  protected def postRequestedForAudit(): RequestPatternBuilder = postRequestedFor(urlEqualTo("/write/audit"))

}
