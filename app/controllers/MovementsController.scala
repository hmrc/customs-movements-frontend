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

import java.time.Instant

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.movements

import scala.concurrent.{ExecutionContext, Future}

class MovementsController @Inject()(
  authenticate: AuthAction,
  connector: CustomsDeclareExportsMovementsConnector,
  mcc: MessagesControllerComponents,
  movementsPage: movements
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private def sort(submissionsWithNotifications: Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]) =
    submissionsWithNotifications.sortBy(_._1.requestTimestamp)(Ordering[Instant].reverse)

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    for {
      submissions <- connector.fetchAllSubmissions()
      notifications <- Future.sequence(submissions.map(submission => connector.fetchNotifications(submission.conversationId)))
      submissionsWithNotifications = submissions.zip(notifications.map(_.sorted.reverse))

    } yield Ok(movementsPage(sort(submissionsWithNotifications)))
  }

}
