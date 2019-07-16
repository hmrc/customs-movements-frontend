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
import controllers.actions.AuthAction
import controllers.storage.CacheIdGenerator.cacheId
import forms.Choice
import forms.Choice.AllowedChoiceValues._
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
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[Choice](cacheId, choiceId)
      .map(data => Ok(choicePage(data.fold(form())(form().fill(_)))))
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
              validChoice.value match {
                case Arrival | Departure => Redirect(controllers.routes.ConsignmentReferencesController.displayPage())
                case AssociateDUCR       => Redirect(controllers.routes.MucrOptionsController.displayPage())
                case DisassociateDUCR    => Redirect(controllers.routes.DisassociateDucrController.displayPage())
                case ShutMucr            => Redirect(controllers.routes.ShutMucrController.displayPage())
                case _                   => Redirect(controllers.routes.ChoiceController.displayChoiceForm())
              }
          }
      )
  }
}
