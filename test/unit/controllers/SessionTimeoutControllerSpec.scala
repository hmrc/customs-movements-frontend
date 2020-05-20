package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.session_timed_out

import scala.concurrent.ExecutionContext.global

class SessionTimeoutControllerSpec extends ControllerLayerSpec {

  val mcc = stubMessagesControllerComponents()
  val startPage = mock[session_timed_out]

  val controller = new SessionTimeoutController(SuccessfulAuth(), mcc, startPage)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(startPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(startPage)
    super.afterEach()
  }

  "Start Controller" should {

    "return 200" when {

      "display signed out method is invoked" in {

        val result = controller.signedOut()(getRequest)

        status(result) must be(OK)
      }
    }

    "return 303" when {

      "display sign out method is invoked" in {

        val result = controller.signOut()(getRequest)

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SessionTimeoutController.signedOut().url))
      }
    }
  }
}
