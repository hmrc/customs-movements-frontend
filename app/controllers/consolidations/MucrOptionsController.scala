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

package controllers.consolidations

import controllers.actions.{AuthAction, JourneyAction}
import controllers.storage.CacheIdGenerator.movementCacheId
import forms.MucrOptions
import forms.MucrOptions.{form, formId}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.mucr_options

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MucrOptionsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  cacheService: CustomsCacheService,
  mucrOptionsPage: mucr_options
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService
      .fetchAndGetEntry[MucrOptions](movementCacheId(), formId)
      .map(data => Ok(mucrOptionsPage(data.fold(form)(form.fill))))
  }

  def save(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(mucrOptionsPage(formWithErrors))),
        formData => {
          val validatedForm = MucrOptions.validateForm(form.fill(formData))
          if (validatedForm.hasErrors) {
            Future.successful(BadRequest(mucrOptionsPage(validatedForm)))
          } else {
            cacheService.cache[MucrOptions](movementCacheId(), formId, formData).map { _ =>
              Redirect(routes.AssociateDucrController.displayPage())
            }
          }
        }
      )
  }
}
