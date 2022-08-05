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

package views

import controllers.CSRFSupport
import models.{SignedInUser, UcrBlock}
import models.cache.{Answers, Cache}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData.validEori
import uk.gov.hmrc.auth.core.Enrolments
import views.spec.ViewMatchers

class ViewSpec extends AnyWordSpec with Matchers with ViewMatchers with MessagesStub with CSRFSupport {

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())

  protected def journeyRequest(answers: Answers, queryUcr: Option[UcrBlock] = None) =
    JourneyRequest(
      Cache(validEori, Some(answers), queryUcr, None),
      AuthenticatedRequest(FakeRequest().withCSRFToken, SignedInUser(validEori, Enrolments(Set.empty)))
    )

  /*
    Implicit Utility class for retrieving common elements which are on the vast majority of pages
   */
  protected implicit class CommonElementFinder(html: Html) {
    private val document = htmlBodyOf(html)

    def getTitle: Element = document.getElementsByTag("title").first()

    def getBackButton: Option[Element] = Option(document.getElementById("back-link"))

    def getSubmitButton: Option[Element] = Option(document.getElementsByClass("govuk-button").first())

    def getErrorSummary: Option[Element] = Option(document.getElementById("error-summary"))

    def getGovUkErrorSummary = document.getElementsByClass("govuk-error-summary")

    def getForm: Option[Element] = Option(document.getElementsByTag("form")).filter(!_.isEmpty).map(_.first())
  }

}
