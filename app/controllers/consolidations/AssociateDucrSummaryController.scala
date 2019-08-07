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

package controllers.consolidations

import controllers.actions.{AuthAction, JourneyAction}
import controllers.exception.IncompleteApplication
import controllers.storage.CacheIdGenerator.movementCacheId
import controllers.storage.FlashKeys
import forms.{AssociateDucr, MucrOptions}
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{CustomsCacheService, SubmissionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ducr_summary

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AssociateDucrSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  cacheService: CustomsCacheService,
  submissionService: SubmissionService,
  associateDucrSummaryPage: associate_ducr_summary
) extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass)

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    for {
      m: Option[MucrOptions] <- cacheService.fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId)
      mucrOptions = m.getOrElse(throw IncompleteApplication)

      a: Option[AssociateDucr] <- cacheService.fetchAndGetEntry[AssociateDucr](movementCacheId(), AssociateDucr.formId)
      associateDucr = a.getOrElse(throw IncompleteApplication)
    } yield Ok(associateDucrSummaryPage(associateDucr, mucrOptions.mucr))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    for {
      m: Option[MucrOptions] <- cacheService.fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId)
      mucrOptions = m.getOrElse(throw IncompleteApplication)

      a: Option[AssociateDucr] <- cacheService.fetchAndGetEntry[AssociateDucr](movementCacheId(), AssociateDucr.formId)
      associateDucr = a.getOrElse(throw IncompleteApplication)

      submissionResult <- submissionService.submitDucrAssociation(mucrOptions, associateDucr)
      _ <- cacheService.remove(movementCacheId())

      result = submissionResult match {
        case ACCEPTED =>
          Redirect(routes.AssociateDucrConfirmationController.displayPage())
            .flashing(FlashKeys.MUCR -> mucrOptions.mucr)
        case _ => handleError("Unable to submit Association Consolidation request")
      }
    } yield result
  }

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    logger.error(logMessage)
    errorHandler.getInternalServerErrorPage()
  }
}