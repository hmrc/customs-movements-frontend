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

package controllers

import controllers.actions.{AuthAction, DucrPartsAction, JourneyRefiner, NonIleQueryAction}
import forms.DucrPartChiefChoice
import forms.DucrPartChiefChoice.form
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.JourneyType.JourneyType
import models.cache._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ducr_part_chief

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DucrPartChiefController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  isDucrPartsFeatureEnabled: DucrPartsAction,
  ileQueryFeatureDisabled: NonIleQueryAction,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  ducrPartChiefPage: ducr_part_chief
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val requiredActions = authenticate andThen isDucrPartsFeatureEnabled andThen ileQueryFeatureDisabled andThen getJourney(
    JourneyType.ARRIVE,
    JourneyType.DEPART,
    JourneyType.ASSOCIATE_UCR,
    JourneyType.DISSOCIATE_UCR
  )

  def displayPage(): Action[AnyContent] =
    requiredActions { implicit request =>
      val choice = request.cache.ducrPartChiefChoice
      Ok(buildPage(choice.fold(form())(form().fill(_))))
    }

  def submit(): Action[AnyContent] =
    requiredActions.async { implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[DucrPartChiefChoice]) => Future.successful(BadRequest(buildPage(formWithErrors))),
          choice => updateCache(request.cache, choice)
        )
    }

  private def buildPage(form: Form[DucrPartChiefChoice])(implicit request: JourneyRequest[_]) =
    ducrPartChiefPage(form)

  private def updateCache(cache: Cache, choice: DucrPartChiefChoice): Future[Result] = {
    val toUpdate = (if (choice.isDucrPart) cache else cache.copy(queryUcr = None)).copy(ducrPartChiefChoice = Some(choice))
    cacheRepository.upsert(toUpdate).map(_ => Redirect(nextPage(choice, cache.answers.map(_.`type`).getOrElse(throw ReturnToStartException))))
  }

  private def nextPage(choice: DucrPartChiefChoice, journeyType: JourneyType) =
    if (choice.choice == DucrPartChiefChoice.IsDucrPart)
      controllers.routes.DucrPartDetailsController.displayPage()
    else
      journeyType match {
        case JourneyType.ARRIVE | JourneyType.DEPART => controllers.routes.ConsignmentReferencesController.displayPage()
        case JourneyType.ASSOCIATE_UCR               => consolidations.routes.MucrOptionsController.displayPage()
        case JourneyType.DISSOCIATE_UCR              => consolidations.routes.DisassociateUcrController.displayPage()
      }

}
