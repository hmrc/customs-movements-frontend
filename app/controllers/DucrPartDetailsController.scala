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

import config.IleQueryConfig
import controllers.actions.{AuthAction, JourneyRefiner, NonIleQueryAction}
import controllers.navigation.Navigator
import forms._
import models.UcrBlock
import models.cache._
import models.requests.{JourneyRequest, RequestWithAnswers}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ducr_part_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DucrPartDetailsController @Inject() (
  mcc: MessagesControllerComponents,
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  ileQueryFeatureDisabled: NonIleQueryAction,
  ileQueryConfig: IleQueryConfig,
  cacheRepository: CacheRepository,
  ducrPartsDetailsPage: ducr_part_details,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] =
    if (ileQueryConfig.isIleQueryEnabled)
      authenticate.async { implicit request =>
        cacheLookup
      }
    else {
      submitDucrPartForJourneyActions.async { implicit request =>
        cacheLookup
      }
    }

  private def cacheLookup(implicit request: RequestWithAnswers[_]): Future[Result] =
    cacheRepository
      .findByEori(request.eori)
      .map {
        case Some(cache) if cache.queryUcr.exists(_.ucrType == UcrType.DucrPart.codeValue) =>
          cache.queryUcr.map(ucrBlock => getEmptyForm.fill(DucrPartDetails(ucrBlock))).getOrElse(getEmptyForm)

        case _ => getEmptyForm
      }
      .map(form => Ok(ducrPartsDetailsPage(form)))

  def submitDucrPartDetails(): Action[AnyContent] = authenticate.async { implicit request =>
    getEmptyForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(ducrPartsDetailsPage(formWithErrors))),
        validDucrPartDetails =>
          cacheRepository.upsert(Cache(request.eori, validDucrPartDetails.toUcrBlock)).map { _ =>
            navigator.continueTo(controllers.routes.ChoiceController.displayChoiceForm())
          }
      )
  }

  private val submitDucrPartForJourneyActions = authenticate andThen ileQueryFeatureDisabled andThen getJourney(
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

  private def handleArrival(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]): Future[Result] =
    saveAndContinue(
      request.cache
        .copy(
          queryUcr = ucrBlock,
          answers = Some(request.answersAs[ArrivalAnswers].copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply)))
        ),
      controllers.routes.SpecificDateTimeController.displayPage()
    )

  private def handleDeparture(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]): Future[Result] =
    saveAndContinue(
      request.cache
        .copy(
          queryUcr = ucrBlock,
          answers = Some(request.answersAs[DepartureAnswers].copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply)))
        ),
      controllers.routes.SpecificDateTimeController.displayPage()
    )

  private def handleAssociate(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]): Future[Result] =
    saveAndContinue(
      request.cache
        .copy(queryUcr = ucrBlock, answers = Some(request.answersAs[AssociateUcrAnswers].copy(associateUcr = ucrBlock.map(AssociateUcr.apply)))),
      consolidations.routes.MucrOptionsController.displayPage()
    )

  private def handleDissociate(ucrBlock: Option[UcrBlock])(implicit request: JourneyRequest[_]): Future[Result] =
    saveAndContinue(
      request.cache
        .copy(queryUcr = ucrBlock, answers = Some(request.answersAs[DisassociateUcrAnswers].copy(ucr = ucrBlock.map(DisassociateUcr.apply)))),
      controllers.consolidations.routes.DisassociateUcrSummaryController.displayPage()
    )

  private def saveAndContinue(cache: Cache, nextPage: Call): Future[Result] =
    cacheRepository
      .upsert(cache)
      .map(_ => Redirect(nextPage))

  private def getEmptyForm: Form[DucrPartDetails] = DucrPartDetails.form()
}
