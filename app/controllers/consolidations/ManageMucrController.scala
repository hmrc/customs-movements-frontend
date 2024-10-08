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
import controllers.consolidations.routes.MucrOptionsController
import controllers.exception.InvalidFeatureStateException
import controllers.navigation.Navigator
import forms.ManageMucrChoice.{form, AssociateAnotherMucr, AssociateThisMucr}
import forms.{AssociateUcr, MucrOptions, UcrType}
import models.cache.AssociateUcrAnswers
import models.cache.JourneyType.ASSOCIATE_UCR
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.consolidations.manage_mucr

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManageMucrController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  page: manage_mucr,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  private val journeyActions = authenticate andThen getJourney(ASSOCIATE_UCR)

  val displayPage: Action[AnyContent] = journeyActions { implicit request =>
    if (request.cache.ucrBlock.map(_.isNot(UcrType.Mucr)).getOrElse(throw InvalidFeatureStateException))
      Redirect(MucrOptionsController.displayPage)
    else {
      val mucrOptions = request.answersAs[AssociateUcrAnswers].manageMucrChoice
      Ok(page(mucrOptions.fold(form)(form.fill), request.cache.ucrBlock))
    }
  }

  val submit: Action[AnyContent] = journeyActions.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(page(formWithErrors, request.cache.ucrBlock))),
        validForm => {
          val answers = request.answersAs[AssociateUcrAnswers]
          val previousManageMucrChoice = answers.manageMucrChoice
          val newManageMucrChoice = Some(validForm)
          val updatedAnswers = answers.copy(manageMucrChoice = newManageMucrChoice)

          (validForm.choice: @unchecked) match {

            case AssociateThisMucr =>
              // Here we need to create AssociateUCR from query and clear MucrOptions if ManageMucrChoice has changed
              val updatedForAssociateThisMucr =
                if (newManageMucrChoice == previousManageMucrChoice) updatedAnswers
                else updatedAnswers.copy(associateUcr = request.cache.ucrBlock.map(AssociateUcr.apply), mucrOptions = None)

              cacheRepository.upsert(request.cache.update(updatedForAssociateThisMucr)).map { _ =>
                navigator.continueTo(MucrOptionsController.displayPage)
              }

            case AssociateAnotherMucr =>
              // Here we need to clear AssociateUCR and create MucrOptions from query if ManageMucrChoice has changed
              val updatedForAssociateAnotherMucr =
                if (newManageMucrChoice == previousManageMucrChoice) updatedAnswers
                else updatedAnswers.copy(associateUcr = None, mucrOptions = request.cache.ucrBlock.map(MucrOptions.apply))

              cacheRepository.upsert(request.cache.update(updatedForAssociateAnotherMucr)).map { _ =>
                navigator.continueTo(routes.AssociateUcrController.displayPage)
              }
          }
        }
      )
  }
}
