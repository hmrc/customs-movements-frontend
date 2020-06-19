package views

import base.{Injector, OverridableInjector}
import config.AppConfig
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import views.html.components.gds.siteHeader

class SiteHeaderViewSpec extends ViewSpec with BeforeAndAfterEach with MockitoSugar with Injector {

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))
  private implicit val request = FakeRequest().withCSRFToken

  override def afterEach(): Unit = {
    reset(appConfig)
    super.afterEach()
  }

  private def headerComponent = injector.instanceOf[siteHeader]

  "SiteHeader component" should {

    "render service name with link to 'Choice' page" in {

      when(appConfig.ileQueryEnabled).thenReturn(false)

      val serviceNameLink = headerComponent()
        .getElementsByClass("govuk-header__link--service-name")
        .first()
      serviceNameLink must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
      serviceNameLink must containMessage("service.name")
    }

    "render service name with link to 'Find Consignment' page" in {

      when(appConfig.ileQueryEnabled).thenReturn(true)

      val serviceNameLink = headerComponent()
        .getElementsByClass("govuk-header__link--service-name")
        .first()
      serviceNameLink must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      serviceNameLink must containMessage("service.name")
    }
  }
}
