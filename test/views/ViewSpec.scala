/*
 * Copyright 2024 HM Revenue & Customs
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
import models.cache.{Answers, Cache}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import models.{SignedInUser, UcrBlock}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData.validEori
import uk.gov.hmrc.auth.core.Enrolments
import views.spec.ViewMatchers

class ViewSpec extends AnyWordSpec with Matchers with ViewMatchers with MessagesStub with CSRFSupport with OptionValues {

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())

  protected val signedInUser = SignedInUser(validEori, Enrolments(Set.empty))

  def checkSaveAndReturnToSummaryButtonIsHidden(view: Html): Unit =
    s"hide 'Save and return to summary' button" in {
      view.getSaveAndReturnButton must not be defined
    }

  def removeBlanksIfAnyBeforeDot(s: String): String = s.replace(" .", ".")

  /*
    Implicit Utility class for retrieving common elements which are on the vast majority of pages
   */
  protected implicit class CommonElementFinder(html: Html) {
    private val document = htmlBodyOf(html)

    def getTitle: Element = document.getElementsByTag("title").first()

    def getBackButton: Option[Element] = Option(document.getElementById("back-link"))

    def getSubmitButton: Option[Element] = Option(document.getElementsByClass("govuk-button").first())

    def getSaveAndReturnButton: Option[Element] =
      Option(document.getElementsByClass("govuk-button--secondary").first())

    def getErrorSummary: Option[Element] = Option(document.getElementById("error-summary"))

    def getGovUkErrorSummary: Elements = document.getElementsByClass("govuk-error-summary")

    def getForm: Option[Element] = Option(document.getElementsByTag("form")).filter(!_.isEmpty).map(_.first())
  }

  def checkAllSaveButtonsAreDisplayed(view: Html)(implicit messages: Messages): Unit = {
    checkSaveAndContinueButtonIsDisplayed(view)
    checkSaveAndReturnToSummaryButtonIsDisplayed(view)
  }

  def checkSaveAndContinueButtonIsDisplayed(view: Html)(implicit messages: Messages): Unit =
    "display 'Save and continue' button" in {
      view.getSubmitButton.value must containMessage("site.continue")
    }

  def checkSaveAndReturnToSummaryButtonIsDisplayed(view: Html)(implicit messages: Messages): Unit =
    s"display 'Save and return to summary' button" in {
      view.getSaveAndReturnButton.value must containMessage("site.saveAndReturnToSummary")
    }

  protected def journeyRequest(
    answers: Answers,
    ucrBlock: Option[UcrBlock] = None,
    ucrBlockFromIleQuery: Boolean = false
  ): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(
      Cache(validEori, Some(answers), ucrBlock, ucrBlockFromIleQuery, None),
      AuthenticatedRequest(FakeRequest().withCSRFToken, signedInUser)
    )

  protected def journeyRequest(cache: Cache): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(cache, AuthenticatedRequest(FakeRequest().withCSRFToken, signedInUser))
}
