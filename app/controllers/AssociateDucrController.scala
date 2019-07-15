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
import controllers.exception.IncompleteApplication
import controllers.storage.CacheIdGenerator.movementCacheId
import forms.AssociateDucr.form
import forms.{AssociateDucr, MucrOptions}
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ducr

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AssociateDucrController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  cacheService: CustomsCacheService,
  associateDucrPage: associate_ducr
)(implicit appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService.fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId).map {
      case Some(options) => Ok(associateDucrPage(form, options.mucr))
      case None          => throw IncompleteApplication
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          cacheService.fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId).map {
            case Some(options) => BadRequest(associateDucrPage(formWithErrors, options.mucr))
            case None          => throw IncompleteApplication
        },
        formData =>
          cacheService.cache(movementCacheId(), AssociateDucr.formId, formData).map { _ =>
            Redirect(routes.AssociateDucrSummaryController.displayPage())
        }
      )
  }
}
