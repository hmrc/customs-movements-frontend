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

package controllers

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.notifications.Notification
import models.submissions.Submission
import models.viewmodels.notificationspage.{NotificationPageSingleElementFactory, NotificationsPageSingleElement}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.notifications

import scala.concurrent.ExecutionContext

class NotificationsController @Inject() (
  authenticate: AuthAction,
  connector: CustomsDeclareExportsMovementsConnector,
  factory: NotificationPageSingleElementFactory,
  mcc: MessagesControllerComponents,
  notifications: notifications
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def listOfNotifications(conversationId: String): Action[AnyContent] = authenticate.async { implicit request =>
    val eori = request.user.eori

    val params = for {
      submission: Option[Submission] <- connector.fetchSingleSubmission(conversationId, eori)
      submissionElement: Option[NotificationsPageSingleElement] = submission.map(factory.build)

      submissionNotifications: Seq[Notification] <- connector.fetchNotifications(conversationId, eori)
      notificationElements: Seq[NotificationsPageSingleElement] = submissionNotifications.sorted.map(factory.build)

      submissionUcr: Option[String] = submission.flatMap(extractUcr)
    } yield (submissionUcr, submissionElement, notificationElements)

    params.map {
      case (Some(submissionUcr), Some(submissionElement), notificationElements) =>
        val elementsToDisplay = submissionElement +: notificationElements
        Ok(notifications(submissionUcr, elementsToDisplay.reverse))

      case _ =>
        Redirect(routes.SubmissionsController.displayPage())
    }
  }

  private def extractUcr(submission: Submission): Option[String] =
    if (submission.hasMucr) submission.extractMucr else submission.extractFirstUcr

}
