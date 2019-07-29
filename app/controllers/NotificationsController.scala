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
