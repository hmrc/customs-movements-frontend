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

import controllers.actions.AuthAction
import controllers.storage.CacheIdGenerator.cacheId
import forms.Choice
import forms.Choice._
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject()(
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[Choice](cacheId, choiceId)
      .map(data => Ok(choicePage(data.fold(form())(form().fill(_)))))
  }

  def startSpecificJourney(choice: String): Action[AnyContent] = authenticate.async { implicit request =>
    val correctChoice = Choice(choice)

    customsCacheService.cache[Choice](cacheId, choiceId, correctChoice).map { _ =>
      correctChoice match {
        case Arrival | Departure => Redirect(controllers.routes.ConsignmentReferencesController.displayPage())
        case AssociateUCR       => Redirect(controllers.consolidations.routes.MucrOptionsController.displayPage())
        case DisassociateUCR    => Redirect(controllers.consolidations.routes.DisassociateUcrController.displayPage())
        case ShutMUCR            => Redirect(controllers.consolidations.routes.ShutMucrController.displayPage())
        case Submissions         => Redirect(controllers.routes.MovementsController.displayPage())
      }
    }
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))),
        validChoice =>
          customsCacheService
            .cache[Choice](cacheId, choiceId, validChoice)
            .map { _ =>
              validChoice match {
                case Arrival | Departure => Redirect(routes.ConsignmentReferencesController.displayPage())
                case AssociateUCR =>
                  Redirect(consolidations.routes.MucrOptionsController.displayPage())
                case DisassociateUCR =>
                  Redirect(consolidations.routes.DisassociateUcrController.displayPage())
                case ShutMUCR    => Redirect(consolidations.routes.ShutMucrController.displayPage())
                case Submissions => Redirect(controllers.routes.MovementsController.displayPage())
              }
          }
      )
  }
}
