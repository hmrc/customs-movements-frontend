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

package controllers.navigation

import base.UnitSpec
import controllers.consolidations.routes.{ArriveOrDepartSummaryController, AssociateUcrSummaryController}
import forms.SaveAndReturnToSummary
import models.SignedInUser
import models.cache.{AssociateUcrAnswers, Cache}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testdata.CommonTestData.validEori
import uk.gov.hmrc.auth.core.Enrolments

class NavigatorSpec extends UnitSpec {

  private val navigator = new Navigator()

  "Navigator.continueTo" should {

    "go to the URL provided" when {
      "Save And Continue" in {
        val request = AuthenticatedRequest[AnyContent](FakeRequest(), SignedInUser(validEori, Enrolments(Set.empty)))

        val result = navigator.continueTo(Call("GET", "/url"))(request)

        result.header.status mustBe SEE_OTHER
        result.header.headers.get(LOCATION) mustBe Some("/url")
      }
    }

    "user is ready to submit" should {

      "Go to the summary page when Save and return to summary form action" in {
        val request = AuthenticatedRequest[AnyContent](
          FakeRequest().withFormUrlEncodedBody(SaveAndReturnToSummary.toString -> ""),
          SignedInUser(validEori, Enrolments(Set.empty))
        )

        val result = navigator.continueTo(Call("GET", "/"))(request)

        result.header.status mustBe SEE_OTHER
        result.header.headers.get(LOCATION) mustBe Some(ArriveOrDepartSummaryController.displayPage.url)
      }

      "Go to the associate ucr summary page when Save and return to summary form action" in {
        val request = JourneyRequest(
          Cache(validEori, AssociateUcrAnswers()),
          AuthenticatedRequest[AnyContent](
            FakeRequest().withFormUrlEncodedBody(SaveAndReturnToSummary.toString -> ""),
            SignedInUser(validEori, Enrolments(Set.empty))
          )
        )

        val result = navigator.continueTo(Call("GET", "/"))(request)

        result.header.status mustBe SEE_OTHER
        result.header.headers.get(LOCATION) mustBe Some(AssociateUcrSummaryController.displayPage.url)
      }
    }
  }
}
