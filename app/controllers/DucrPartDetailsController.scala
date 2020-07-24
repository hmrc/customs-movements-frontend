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

import config.IleQueryConfig
import controllers.actions.{AuthAction, DucrPartsAction, JourneyRefiner, NonIleQueryAction}
import forms._
import javax.inject.Inject
import models.cache._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
  ileQueryConfig: IleQueryConfig,
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

  def submitDucrPartDetailsJourney(): Action[AnyContent] =
    (authenticate andThen isDucrPartsFeatureEnabled andThen ileQueryFeatureDisabled andThen getJourney(
      JourneyType.ARRIVE,
      JourneyType.DEPART,
      JourneyType.ASSOCIATE_UCR,
      JourneyType.DISSOCIATE_UCR
    )).async { implicit request =>
      getEmptyForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(ducrPartsDetailsPage(formWithErrors))),
          validDucrPartDetails => {
            val ucrBlock = Some(validDucrPartDetails.toUcrBlock)
            request.answers.`type` match {
              case JourneyType.ARRIVE =>
                val answers = request.answersAs[ArrivalAnswers]
                cacheRepository
                  .upsert(
                    request.cache
                      .copy(queryUcr = ucrBlock, answers = Some(answers.copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply))))
                  )
                  .map { _ =>
                    Redirect(controllers.routes.SpecificDateTimeController.displayPage())
                  }
              case JourneyType.DEPART =>
                val answers = request.answersAs[DepartureAnswers]
                cacheRepository
                  .upsert(
                    request.cache
                      .copy(queryUcr = ucrBlock, answers = Some(answers.copy(consignmentReferences = ucrBlock.map(ConsignmentReferences.apply))))
                  )
                  .map { _ =>
                    Redirect(controllers.routes.SpecificDateTimeController.displayPage())
                  }
              case JourneyType.ASSOCIATE_UCR =>
                val answers = request.answersAs[AssociateUcrAnswers]
                cacheRepository
                  .upsert(
                    request.cache
                      .copy(queryUcr = ucrBlock, answers = Some(answers.copy(associateUcr = ucrBlock.map(AssociateUcr.apply))))
                  )
                  .map { _ =>
                    Redirect(consolidations.routes.MucrOptionsController.displayPage())
                  }
              case JourneyType.DISSOCIATE_UCR =>
                val answers = request.answersAs[DisassociateUcrAnswers]
                cacheRepository
                  .upsert(
                    request.cache
                      .copy(queryUcr = ucrBlock, answers = Some(answers.copy(ucr = ucrBlock.map(DisassociateUcr.apply))))
                  )
                  .map { _ =>
                    Redirect(controllers.consolidations.routes.DisassociateUcrSummaryController.displayPage())
                  }
            }
          }
        )
    }

  private def getEmptyForm: Form[DucrPartDetails] = DucrPartDetails.form()

}
