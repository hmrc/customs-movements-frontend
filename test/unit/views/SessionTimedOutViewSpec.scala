package views

import base.Injector
import models.cache.AssociateUcrAnswers
import play.api.i18n.MessagesApi
import play.twirl.api.Html
import views.html.session_timed_out

class SessionTimedOutViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(AssociateUcrAnswers())
  private val page = instanceOf[session_timed_out]
  private def createView(): Html = page()

  "SessionTimedOut View" should {

    "have proper messages for labels" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("sessionTimout.title")
      messages must haveTranslationFor("sessionTimout.paragraph.saved")
      messages must haveTranslationFor("sessionTimout.signin.button")
      messages must haveTranslationFor("sessionTimout.back.button")
    }

    val view = createView()

    "display same page header" in {

      view.getElementsByTag("h1").text() mustBe messages("sessionTimout.title")
    }

    "display sign-in button" in {

      val button = view.getElementsByClass("govuk-button").first()

      button.text() mustBe messages("sessionTimout.signin.button")
      button.attr("href") mustBe controllers.routes.ChoiceController.displayChoiceForm().url
    }

    "display back to gov.uk link" in {

      val link = view.getElementsByClass("govuk-link").first()

      link.text() mustBe messages("sessionTimout.back.button")
      link.attr("href") mustBe "https://www.gov.uk/"
    }
  }
}
