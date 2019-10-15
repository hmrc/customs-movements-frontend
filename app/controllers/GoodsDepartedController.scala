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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.storage.CacheIdGenerator.movementCacheId
import forms.Choice.Departure
import forms.GoodsDeparted._
import forms.GoodsDeparted
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.goods_departed

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsDepartedController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  goodsDepartedPage: goods_departed
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice match {
      case Departure =>
        customsCacheService
          .fetchAndGetEntry[GoodsDeparted](movementCacheId, formId)
          .map(data => Ok(goodsDepartedPage(data.fold(form)(form.fill(_)))))
      case _ => Future.successful(errorHandler.getBadRequestPage())
    }
  }

  def saveGoodsDeparted(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[GoodsDeparted]) => Future.successful(BadRequest(goodsDepartedPage(formWithErrors))),
        validForm =>
          customsCacheService.cache[GoodsDeparted](movementCacheId, formId, validForm).map { _ =>
            if (validForm.departedPlace == AllowedPlaces.outOfTheUk)
              Redirect(controllers.routes.TransportController.displayPage())
            else Redirect(controllers.routes.SummaryController.displayPage())
        }
      )
  }
}
