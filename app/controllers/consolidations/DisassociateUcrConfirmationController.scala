/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.consolidations

import controllers.actions.AuthAction
import controllers.storage.FlashKeys
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.JourneyType
import models.cache.JourneyType.{DISSOCIATE_UCR, JourneyType}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.confirmation_page

import scala.concurrent.ExecutionContext

@Singleton
class DisassociateUcrConfirmationController @Inject()(
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  confirmationPage: confirmation_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = authenticate { implicit request =>
    val journeyType = extractJourneyType
    journeyType match {
      case DISSOCIATE_UCR => Ok(confirmationPage(journeyType))
      case _              => throw ReturnToStartException
    }
  }

  private def extractJourneyType(implicit request: Request[_]): JourneyType =
    request.flash.get(FlashKeys.MOVEMENT_TYPE).map(JourneyType.withName).getOrElse(throw ReturnToStartException)

}
