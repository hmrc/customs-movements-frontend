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

package services

import base.ExportsTestData._
import base.{MovementBaseSpec, TestDataHelper}
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import org.joda.time.DateTime
import play.api.http.Status.{ACCEPTED, INTERNAL_SERVER_ERROR}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.Authorization

class SubmissionServiceSpec extends MovementBaseSpec {

  implicit val hc: HeaderCarrier =
    HeaderCarrier(
      authorization = Some(Authorization(TestDataHelper.createRandomString(255))),
      nsStamp = DateTime.now().getMillis
    )
  val submissionService = new SubmissionService(mockCustomsCacheService, mockCustomsExportsMovementConnector, metrics)

  "SubmissionService" should {
    "Submit valid Movement Request" in {
      withCacheMap(Some(CacheMap(Arrival, cacheMapData(Arrival))))
      sendMovementRequest202Response
      val result = submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue
      result mustBe ACCEPTED
    }
    "handle failure when No data" in {
      withCacheMap(None)
      sendMovementRequest202Response
      val result = submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue
      result mustBe INTERNAL_SERVER_ERROR
    }
  }

}
