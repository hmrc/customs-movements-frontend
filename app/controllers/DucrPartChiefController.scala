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

import controllers.actions.{AuthAction, JourneyRefiner, NonIleQueryAction}
import forms.DucrPartChiefChoice
import forms.DucrPartChiefChoice.form
import javax.inject.{Inject, Singleton}
import models.cache.JourneyType.JourneyType
import models.cache._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.ducr_part_chief

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DucrPartChiefController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  ileQueryFeatureDisabled: NonIleQueryAction,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  ducrPartChiefPage: ducr_part_chief
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen ileQueryFeatureDisabled andThen getJourney(
      JourneyType.ARRIVE,
      JourneyType.DEPART,
      JourneyType.ASSOCIATE_UCR,
      JourneyType.DISSOCIATE_UCR
    )) { implicit request =>
      val choice = request.answersAs[DucrPartChiefAnswers].ducrPartChiefChoice
      Ok(buildPage(choice.fold(form())(form().fill(_))))
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen ileQueryFeatureDisabled andThen getJourney(
      JourneyType.ARRIVE,
      JourneyType.DEPART,
      JourneyType.ASSOCIATE_UCR,
      JourneyType.DISSOCIATE_UCR
    )).async { implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[DucrPartChiefChoice]) => Future.successful(BadRequest(buildPage(formWithErrors))),
          choice => {
            request.answers match {
              case arrivalAnswers: ArrivalAnswers =>
                cache.upsert(request.cache.update(updateArrivalAnswers(arrivalAnswers, choice))).map { _ =>
                  Redirect(nextPage(choice, JourneyType.ARRIVE))
                }
              case departureAnswers: DepartureAnswers =>
                cache.upsert(request.cache.update(updateDepartureAnswers(departureAnswers, choice))).map { _ =>
                  Redirect(nextPage(choice, JourneyType.DEPART))
                }
              case associateUcrAnswers: AssociateUcrAnswers =>
                cache.upsert(request.cache.update(updateAssociateUcrAnswers(associateUcrAnswers, choice))).map { _ =>
                  Redirect(nextPage(choice, JourneyType.ASSOCIATE_UCR))
                }
              case disassociateUcrAnswers: DisassociateUcrAnswers =>
                cache.upsert(request.cache.update(updateDisassociateUcrAnswers(disassociateUcrAnswers, choice))).map { _ =>
                  Redirect(nextPage(choice, JourneyType.DISSOCIATE_UCR))
                }
            }
          }
        )
    }

  private def buildPage(form: Form[DucrPartChiefChoice])(implicit request: JourneyRequest[_]) =
    ducrPartChiefPage(form)

  private def updateArrivalAnswers(arrivalAnswers: ArrivalAnswers, choice: DucrPartChiefChoice): ArrivalAnswers =
    arrivalAnswers.copy(ducrPartChiefChoice = Some(choice))

  private def updateDepartureAnswers(departureAnswers: DepartureAnswers, choice: DucrPartChiefChoice): DepartureAnswers =
    departureAnswers.copy(ducrPartChiefChoice = Some(choice))

  private def updateAssociateUcrAnswers(associateUcrAnswers: AssociateUcrAnswers, choice: DucrPartChiefChoice): AssociateUcrAnswers =
    associateUcrAnswers.copy(ducrPartChiefChoice = Some(choice))

  private def updateDisassociateUcrAnswers(disassociateUcrAnswers: DisassociateUcrAnswers, choice: DucrPartChiefChoice): DisassociateUcrAnswers =
    disassociateUcrAnswers.copy(ducrPartChiefChoice = Some(choice))

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
