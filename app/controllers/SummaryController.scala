/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.actions.{AuthAction, LegacyJourneyAction}
import controllers.storage.CacheIdGenerator.movementCacheId
import controllers.storage.FlashKeys
import forms.Choice._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomsCacheService, LegacySubmissionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
                                   authenticate: AuthAction,
                                   journeyType: LegacyJourneyAction,
                                   errorHandler: ErrorHandler,
                                   customsCacheService: CustomsCacheService,
                                   submissionService: LegacySubmissionService,
                                   mcc: MessagesControllerComponents,
                                   arrivalSummaryPage: arrival_summary_page,
                                   departureSummaryPage: departure_summary_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass)

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val cacheMapOpt = customsCacheService.fetch(movementCacheId)

    val typeOfJourney = request.choice

    cacheMapOpt.map {
      case Some(data) if typeOfJourney == Arrival =>
        Ok(arrivalSummaryPage(data))
      case Some(data) if typeOfJourney == Departure =>
        Ok(departureSummaryPage(data))
      case _ =>
        logger.warn(s"No movement data found in cache.")
        errorHandler.getInternalServerErrorPage
    }
  }

  def submitMovementRequest(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    submissionService
      .submitMovementRequest(movementCacheId, request.authenticatedRequest.user.eori, request.choice)
      .flatMap {
        case (Some(consignmentReferences), ACCEPTED) =>
          customsCacheService.remove(movementCacheId).map { _ =>
            Redirect(routes.MovementConfirmationController.display())
              .flashing(
                FlashKeys.MOVEMENT_TYPE -> request.choice.value,
                FlashKeys.UCR_KIND -> consignmentReferences.reference,
                FlashKeys.UCR -> consignmentReferences.referenceValue
              )
          }
        case _ =>
          Future.successful {
            logger.warn(s"No movement data found in cache.")
            errorHandler.getInternalServerErrorPage
          }
      }
  }
}
