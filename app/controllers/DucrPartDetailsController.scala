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

import controllers.actions.{AuthAction, DucrPartsAction, JourneyRefiner, NonIleQueryAction}
import forms._
import javax.inject.Inject
import models.UcrBlock
import models.cache._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.ducr_part_details

import scala.concurrent.{ExecutionContext, Future}

class DucrPartDetailsController @Inject()(
  mcc: MessagesControllerComponents,
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  isDucrPartsFeatureEnabled: DucrPartsAction,
  ileQueryFeatureDisabled: NonIleQueryAction,
  cacheRepository: CacheRepository,
  ducrPartsDetailsPage: ducr_part_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen isDucrPartsFeatureEnabled).async { implicit request =>
    cacheRepository
      .findByEori(request.eori)
      .map {
        case Some(cache) if cache.queryUcr.exists(_.ucrType == UcrType.DucrPart.codeValue) =>
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

  private val submitDucrPartForJourneyActions = authenticate andThen isDucrPartsFeatureEnabled andThen ileQueryFeatureDisabled andThen getJourney(
    JourneyType.ARRIVE,
    JourneyType.DEPART,
    JourneyType.ASSOCIATE_UCR,
    JourneyType.DISSOCIATE_UCR
  )

  def submitDucrPartDetailsJourney(): Action[AnyContent] =
    submitDucrPartForJourneyActions.async { implicit request =>
      getEmptyForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(ducrPartsDetailsPage(formWithErrors))),
          validDucrPartDetails => {
            val ucrBlock = Some(validDucrPartDetails.toUcrBlock)
            request.answers.`type` match {
              case JourneyType.ARRIVE         => handleArrival(ucrBlock)
              case JourneyType.DEPART         => handleDeparture(ucrBlock)
              case JourneyType.ASSOCIATE_UCR  => handleAssociate(ucrBlock)
              case JourneyType.DISSOCIATE_UCR => handleDissociate(ucrBlock)
            }
          }
        )
    }

  private def handleArrival(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]) =
    saveAndContinue(
      request.cache
        .copy(
          queryUcr = ucrBlock,
          answers = Some(request.answersAs[ArrivalAnswers].copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply)))
        ),
      controllers.routes.SpecificDateTimeController.displayPage()
    )

  private def handleDeparture(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]) =
    saveAndContinue(
      request.cache
        .copy(
          queryUcr = ucrBlock,
          answers = Some(request.answersAs[DepartureAnswers].copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply)))
        ),
      controllers.routes.SpecificDateTimeController.displayPage()
    )

  private def handleAssociate(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]) =
    saveAndContinue(
      request.cache
        .copy(queryUcr = ucrBlock, answers = Some(request.answersAs[AssociateUcrAnswers].copy(associateUcr = ucrBlock.map(AssociateUcr.apply)))),
      consolidations.routes.MucrOptionsController.displayPage()
    )

  private def handleDissociate(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]) =
    saveAndContinue(
      request.cache
        .copy(queryUcr = ucrBlock, answers = Some(request.answersAs[DisassociateUcrAnswers].copy(ucr = ucrBlock.map(DisassociateUcr.apply)))),
      controllers.consolidations.routes.DisassociateUcrSummaryController.displayPage()
    )

  private def saveAndContinue(cache: Cache, nextPage: Call) =
    cacheRepository
      .upsert(cache)
      .map(_ => Redirect(nextPage))

  private def getEmptyForm: Form[DucrPartDetails] = DucrPartDetails.form()

}
