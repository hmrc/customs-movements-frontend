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
import controllers.consolidations.routes.AssociateUcrController
import controllers.summary.routes.AssociateUcrSummaryController
import controllers.navigation.Navigator
import forms.MucrOptions
import forms.MucrOptions.form
import models.cache.AssociateUcrAnswers
import models.cache.JourneyType.ASSOCIATE_UCR
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat.Appendable
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.consolidations.mucr_options

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MucrOptionsController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  page: mucr_options,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen getJourney(ASSOCIATE_UCR)) { implicit request =>
    val mucrOptions = request.answersAs[AssociateUcrAnswers].mucrOptions
    Ok(buildPage(mucrOptions.fold(form)(form.fill)))
  }

  val save: Action[AnyContent] = (authenticate andThen getJourney(ASSOCIATE_UCR)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(buildPage(formWithErrors))),
        validForm => {
          val updatedAnswers = request.answersAs[AssociateUcrAnswers].copy(mucrOptions = Some(validForm))
          if (request.cache.ucrBlockFromIleQuery || request.cache.isDucrPartChief)
            cacheRepository.upsert(request.cache.update(updatedAnswers.copy(readyToSubmit = Some(true)))).map { _ =>
              Redirect(AssociateUcrSummaryController.displayPage)
            }
          else
            cacheRepository.upsert(request.cache.update(updatedAnswers)).map { _ =>
              navigator.continueTo(AssociateUcrController.displayPage)
            }
        }
      )
  }

  private def buildPage(form: Form[MucrOptions])(implicit request: JourneyRequest[_]): Appendable =
    page(form, request.cache.ucrBlock, request.answersAs[AssociateUcrAnswers].manageMucrChoice)
}
