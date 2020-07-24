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

package controllers.consolidations

import config.IleQueryConfig
import controllers.actions.{AuthAction, JourneyRefiner}
import forms.MucrOptions
import forms.MucrOptions.form
import javax.inject.{Inject, Singleton}
import models.cache.{AssociateUcrAnswers, DucrPartChiefAnswers, JourneyType}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associateucr.mucr_options

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MucrOptionsController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  ileQueryConfig: IleQueryConfig,
  page: mucr_options
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ASSOCIATE_UCR)) { implicit request =>
    val mucrOptions = request.answersAs[AssociateUcrAnswers].mucrOptions
    Ok(buildPage(mucrOptions.fold(form)(form.fill)))
  }

  def save(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ASSOCIATE_UCR)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(buildPage(formWithErrors))),
        validForm => {
          val validatedForm = MucrOptions.validateForm(form.fill(validForm))
          if (validatedForm.hasErrors) {
            Future.successful(BadRequest(buildPage(validatedForm)))
          } else {
            val updatedAnswers = request.answersAs[AssociateUcrAnswers].copy(mucrOptions = Some(validForm))
            cacheRepository.upsert(request.cache.update(updatedAnswers)).map { _ =>
              if (ileQueryConfig.isIleQueryEnabled || request.answersAs[DucrPartChiefAnswers].isDucrPartChief)
                Redirect(routes.AssociateUcrSummaryController.displayPage())
              else
                Redirect(routes.AssociateUcrController.displayPage())
            }
          }
        }
      )
  }

  private def buildPage(form: Form[MucrOptions])(implicit request: JourneyRequest[_]) =
    page(form, request.cache.queryUcr, request.answersAs[AssociateUcrAnswers].manageMucrChoice)
}
