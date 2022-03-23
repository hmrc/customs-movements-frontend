/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.actions.{AuthAction, JourneyRefiner, NonIleQueryAction}
import forms.DisassociateUcr
import javax.inject.{Inject, Singleton}
import models.cache.{DisassociateUcrAnswers, JourneyType}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.disassociateucr.disassociate_ucr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DisassociateUcrController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  ileQueryFeatureDisabled: NonIleQueryAction,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  page: disassociate_ucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen ileQueryFeatureDisabled andThen getJourney(JourneyType.DISSOCIATE_UCR)) {
    implicit request =>
      request.answersAs[DisassociateUcrAnswers].ucr match {
        case Some(ucr) => Ok(page(DisassociateUcr.form.fill(ucr)))
        case _         => Ok(page(DisassociateUcr.form))
      }
  }

  def submit(): Action[AnyContent] = (authenticate andThen ileQueryFeatureDisabled andThen getJourney(JourneyType.DISSOCIATE_UCR)).async {
    implicit request =>
      DisassociateUcr.form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(page(formWithErrors))),
          validForm => {
            val updatedAnswers = request.answersAs[DisassociateUcrAnswers].copy(ucr = Some(validForm))
            cacheRepository.upsert(request.cache.update(updatedAnswers)).map { _ =>
              Redirect(controllers.consolidations.routes.DisassociateUcrSummaryController.displayPage())
            }
          }
        )
  }
}
