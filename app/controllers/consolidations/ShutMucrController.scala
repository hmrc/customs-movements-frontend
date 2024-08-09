/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.summary.routes.ShutMucrSummaryController
import forms.ShutMucr
import forms.ShutMucr.form
import models.cache.JourneyType.SHUT_MUCR
import models.cache.ShutMucrAnswers
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.consolidations.shut_mucr

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShutMucrController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  shutMucrPage: shut_mucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen getJourney(SHUT_MUCR)) { implicit request =>
    val shutMucr: Option[ShutMucr] = request.answersAs[ShutMucrAnswers].shutMucr
    Ok(shutMucrPage(shutMucr.fold(form())(form().fill)))
  }

  val submitForm: Action[AnyContent] = (authenticate andThen getJourney(SHUT_MUCR)).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(shutMucrPage(formWithErrors))),
        validForm => {
          val updatedAnswers = request.answersAs[ShutMucrAnswers].copy(shutMucr = Some(validForm))
          cacheRepository.upsert(request.cache.update(updatedAnswers)).map { _ =>
            Redirect(ShutMucrSummaryController.displayPage)
          }
        }
      )
  }
}
