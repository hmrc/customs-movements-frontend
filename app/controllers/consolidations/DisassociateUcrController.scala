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

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.consolidations.routes.DisassociateUcrSummaryController
import forms.DisassociateUcr.form
import models.cache.DisassociateUcrAnswers
import models.cache.JourneyType.DISSOCIATE_UCR
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.disassociateucr.disassociate_ucr

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DisassociateUcrController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  page: disassociate_ucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(DISSOCIATE_UCR)) { implicit request =>
    request.answersAs[DisassociateUcrAnswers].ucr match {
      case Some(ucr) => Ok(page(form.fill(ucr)))
      case _         => Ok(page(form))
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(DISSOCIATE_UCR)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(page(formWithErrors))),
        validForm => {
          val updatedAnswers = request.answersAs[DisassociateUcrAnswers].copy(ucr = Some(validForm))
          cacheRepository.upsert(request.cache.update(updatedAnswers)).map { _ =>
            Redirect(DisassociateUcrSummaryController.displayPage())
          }
        }
      )
  }
}
