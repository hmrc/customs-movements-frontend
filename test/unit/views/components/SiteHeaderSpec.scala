package views.components

import base.Injector
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpec
import views.html.components.gds.siteHeader

class SiteHeaderSpec extends ViewSpec with Injector {

  private val page = instanceOf[siteHeader]
  private implicit val request = FakeRequest()

  private def createHeader(): Html = page()(request, messages)

  "Site header" should {
    val siteHeader = createHeader()

    "display banner with the service name" in {
      siteHeader.getElementsByClass("govuk-header__link govuk-header__link--service-name").first() must containMessage("service.name")
    }

  }
}
