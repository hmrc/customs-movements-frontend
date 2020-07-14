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

package controllers

import controllers.actions.{AuthAction, DucrPartsAction}
import forms.{DucrPartDetails, UcrType}
import javax.inject.Inject
import models.cache.Cache
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.ducr_part_details

import scala.concurrent.{ExecutionContext, Future}

class DucrPartDetailsController @Inject()(
  mcc: MessagesControllerComponents,
  authenticate: AuthAction,
  isDucrPartsFeatureEnabled: DucrPartsAction,
  cacheRepository: CacheRepository,
  ducrPartsDetailsPage: ducr_part_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen isDucrPartsFeatureEnabled).async { implicit request =>
    cacheRepository
      .findByEori(request.eori)
      .map {
        case Some(cache) if cache.queryUcr.exists(_.ucrType == UcrType.DucrParts.codeValue) =>
          cache.queryUcr.map(ucrBlock => getEmptyForm.fill(DucrPartDetails(ucrBlock))).getOrElse(getEmptyForm)

        case _ => getEmptyForm
      }
      .map(form => Ok(ducrPartsDetailsPage(form)))
  }

  def submitDucrPartDetails(): Action[AnyContent] = (authenticate andThen isDucrPartsFeatureEnabled).async { implicit request =>
    getEmptyForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(ducrPartsDetailsPage(formWithErrors))),
        validDucrPartDetails =>
          cacheRepository.upsert(Cache(request.eori, validDucrPartDetails.toUcrBlock)).map { _ =>
            Redirect(controllers.routes.ChoiceController.displayChoiceForm())
        }
      )
  }

  private def getEmptyForm: Form[DucrPartDetails] = DucrPartDetails.form()
}
