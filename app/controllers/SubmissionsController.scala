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

package controllers

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthAction
import models.submissions.Submission
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.movements

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmissionsController @Inject() (
  authenticate: AuthAction,
  connector: CustomsDeclareExportsMovementsConnector,
  mcc: MessagesControllerComponents,
  movementsPage: movements
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val displayPage: Action[AnyContent] = authenticate.async { implicit request =>
    for {
      submissions <- connector.fetchAllSubmissions(request.user.eori)
    } yield Ok(movementsPage(sortWithOldestLast(submissions)))
  }

  private def sortWithOldestLast(submissionsWithNotifications: Seq[Submission]): Seq[Submission] =
    submissionsWithNotifications.sortBy(_.requestTimestamp)(Ordering[Instant].reverse)
}
