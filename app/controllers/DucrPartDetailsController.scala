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

package controllers

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.consolidations.routes.MucrOptionsController
import controllers.navigation.Navigator
import controllers.routes.SpecificDateTimeController
import controllers.summary.routes.DisassociateUcrSummaryController
import forms.DucrPartDetails.form
import forms._
import models.UcrBlock
import models.cache.JourneyType._
import models.cache._
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ducr_part_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DucrPartDetailsController @Inject() (
  mcc: MessagesControllerComponents,
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  ducrPartsDetailsPage: ducr_part_details,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  private val journeyActions = authenticate andThen getJourney(ARRIVE, DEPART, ASSOCIATE_UCR, DISSOCIATE_UCR)

  val displayPage: Action[AnyContent] = journeyActions.async { implicit request =>
    cacheRepository
      .findByEori(request.eori)
      .map {
        case Some(cache) if cache.ucrBlock.exists(_.ucrType == UcrType.DucrPart.codeValue) =>
          cache.ucrBlock.map(ucrBlock => form().fill(DucrPartDetails(ucrBlock))).getOrElse(form())

        case _ => form()
      }
      .map(form => Ok(ducrPartsDetailsPage(form)))
  }

  val submitDucrPartDetails: Action[AnyContent] = journeyActions.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(ducrPartsDetailsPage(formWithErrors))),
        validDucrPartDetails => {
          val ucrBlock = Some(validDucrPartDetails.toUcrBlock)
          (request.answers.`type`: @unchecked) match {
            case ARRIVE         => handleArrival(ucrBlock)
            case DEPART         => handleDeparture(ucrBlock)
            case ASSOCIATE_UCR  => handleAssociate(ucrBlock)
            case DISSOCIATE_UCR => handleDissociate(ucrBlock)
          }
        }
      )
  }

  private def handleArrival(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    saveAndContinue(
      request.cache.copy(
        ucrBlock = ucrBlock,
        answers = Some(request.answersAs[ArrivalAnswers].copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply)))
      ),
      SpecificDateTimeController.displayPage
    )

  private def handleDeparture(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    saveAndContinue(
      request.cache.copy(
        ucrBlock = ucrBlock,
        answers = Some(request.answersAs[DepartureAnswers].copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply)))
      ),
      SpecificDateTimeController.displayPage
    )

  private def handleAssociate(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    saveAndContinue(
      request.cache
        .copy(ucrBlock = ucrBlock, answers = Some(request.answersAs[AssociateUcrAnswers].copy(associateUcr = ucrBlock.map(AssociateUcr.apply)))),
      MucrOptionsController.displayPage
    )

  private def handleDissociate(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    saveAndContinue(
      request.cache
        .copy(ucrBlock = ucrBlock, answers = Some(request.answersAs[DisassociateUcrAnswers].copy(ucr = ucrBlock.map(DisassociateUcr.apply)))),
      DisassociateUcrSummaryController.displayPage
    )

  private def saveAndContinue(cache: Cache, call: Call)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    cacheRepository.upsert(cache).map(_ => navigator.continueTo(call))
}
