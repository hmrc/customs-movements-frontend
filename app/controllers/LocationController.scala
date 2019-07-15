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
import controllers.actions.{AuthAction, JourneyAction}
import controllers.storage.CacheIdGenerator.movementCacheId
import forms.Location._
import forms.{Choice, Location}
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.location

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  locationPage: location
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[Location](movementCacheId, formId)
      .map(data => Ok(locationPage(data.fold(form)(form.fill(_)), request.choice.value)))
  }

  def saveLocation(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Location]) =>
          Future.successful(BadRequest(locationPage(formWithErrors, request.choice.value))),
        validForm =>
          customsCacheService
            .cache[Location](movementCacheId(), formId, validForm)
            .map { _ =>
              request.choice match {
                case Choice(Choice.AllowedChoiceValues.Arrival) =>
                  Redirect(controllers.routes.TransportController.displayPage())
                case Choice(Choice.AllowedChoiceValues.Departure) =>
                  Redirect(controllers.routes.GoodsDepartedController.displayPage())
              }
          }
      )
  }
}
