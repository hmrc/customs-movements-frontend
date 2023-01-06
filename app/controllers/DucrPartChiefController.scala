/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.consolidations.routes.{DisassociateUcrController, MucrOptionsController}
import controllers.navigation.Navigator
import controllers.routes.{ConsignmentReferencesController, DucrPartDetailsController}
import forms.DucrPartChiefChoice
import forms.DucrPartChiefChoice.form
import models.ReturnToStartException
import models.cache.JourneyType._
import models.cache._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ducr_part_chief

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DucrPartChiefController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  ducrPartChiefPage: ducr_part_chief,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  private val requiredActions = authenticate andThen getJourney(ARRIVE, DEPART, ASSOCIATE_UCR, DISSOCIATE_UCR)

  val displayPage: Action[AnyContent] = requiredActions { implicit request =>
    val choice = request.cache.ducrPartChiefChoice
    Ok(ducrPartChiefPage(choice.fold(form())(form().fill(_))))
  }

  val submit: Action[AnyContent] = requiredActions.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DucrPartChiefChoice]) => Future.successful(BadRequest(ducrPartChiefPage(formWithErrors))),
        choice => updateCache(request.cache, choice)
      )
  }

  private def updateCache(cache: Cache, choice: DucrPartChiefChoice)(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val toUpdate = (if (choice.isDucrPart) cache else cache.copy(ucrBlock = None)).copy(ducrPartChiefChoice = Some(choice))
    cacheRepository
      .upsert(toUpdate)
      .map(_ => nextPage(choice, cache.answers.map(_.`type`).getOrElse(throw ReturnToStartException)))
  }

  private def nextPage(choice: DucrPartChiefChoice, journeyType: JourneyType)(implicit request: JourneyRequest[AnyContent]): Result =
    if (choice.choice == DucrPartChiefChoice.IsDucrPart)
      navigator.continueTo(DucrPartDetailsController.displayPage)
    else
      (journeyType: @unchecked) match {
        case ARRIVE | DEPART => navigator.continueTo(ConsignmentReferencesController.displayPage)
        case ASSOCIATE_UCR   => navigator.continueTo(MucrOptionsController.displayPage)
        case DISSOCIATE_UCR  => navigator.continueTo(DisassociateUcrController.displayPage)
      }
}
