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
import controllers.exception.IncompleteApplication
import controllers.storage.CacheIdGenerator.movementCacheId
import forms.AssociateUcr.form
import forms.{AssociateUcr, MucrOptions}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ucr

import scala.concurrent.ExecutionContext

@Singleton
class AssociateDucrController @Inject()(
                                         authenticate: AuthAction,
                                         journeyType: JourneyAction,
                                         mcc: MessagesControllerComponents,
                                         cacheService: CustomsCacheService,
                                         associateUcrPage: associate_ucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService.fetch(movementCacheId()).map {
      case Some(cache) =>
        cache.getEntry[MucrOptions](MucrOptions.formId) match {
          case Some(mucr) =>
            val savedDucr = cache.getEntry[AssociateUcr](AssociateUcr.formId)
            Ok(associateUcrPage(savedDucr.fold(form)(form.fill), mucr))
          case None => throw IncompleteApplication
        }
      case None => throw IncompleteApplication
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          println(formWithErrors)
          cacheService.fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId).map {
            case Some(options) => BadRequest(associateUcrPage(formWithErrors, options))
            case None          => throw IncompleteApplication
        }},
        formData =>
          cacheService.cache(movementCacheId(), AssociateUcr.formId, formData).map { _ =>
            Redirect(routes.AssociateDucrSummaryController.displayPage())
        }
      )
  }
}
