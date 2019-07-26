package controllers

import config.AppConfig
import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.notifications

import scala.concurrent.ExecutionContext

class NotificationsController @Inject()(
  authenticate: AuthAction,
  connector: CustomsDeclareExportsMovementsConnector,
  mcc: MessagesControllerComponents,
  notifications: notifications
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) {

  def listOfNotifications(conversationId: String): Action[AnyContent] = authenticate.async { implicit request =>
    connector.fetchNotifications(conversationId).map { notifications =>
      Ok(Json.toJson(notifications))
    }
  }
}
