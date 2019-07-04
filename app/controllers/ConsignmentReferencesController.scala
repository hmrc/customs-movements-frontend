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
import controllers.util.CacheIdGenerator.movementCacheId
import forms.ConsignmentReferences._
import forms.{Choice, ConsignmentReferences}
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.consignment_references

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsignmentReferencesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[ConsignmentReferences](movementCacheId, formId)
      .map(data => Ok(consignmentReferencesPage(data.fold(form)(form.fill(_)))))
  }

  def saveConsignmentReferences(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignmentReferences]) =>
          Future.successful(BadRequest(consignmentReferencesPage(formWithErrors))),
        validForm =>
          customsCacheService
            .cache[ConsignmentReferences](movementCacheId(), formId, validForm)
            .map { _ =>
              request.choice match {
                case Choice(Choice.AllowedChoiceValues.Arrival) =>
                  Redirect(controllers.routes.MovementDetailsController.displayPage())
                case Choice(Choice.AllowedChoiceValues.Departure) =>
                  Redirect(controllers.routes.LocationController.displayPage())
              }
          }
      )
  }
}
