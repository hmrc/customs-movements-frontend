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

import controllers.actions.{AuthAction, LegacyJourneyAction}
import controllers.exception.IncompleteApplication
import controllers.storage.CacheIdGenerator.movementCacheId
import controllers.storage.FlashKeys
import forms.ShutMucr
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CustomsCacheService, LegacySubmissionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.shut_mucr_summary

import scala.concurrent.ExecutionContext
import scala.util.Success

class ShutMucrSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: LegacyJourneyAction,
  mcc: MessagesControllerComponents,
  cacheService: CustomsCacheService,
  submissionService: LegacySubmissionService,
  shutMucrSummaryPage: shut_mucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService.fetchAndGetEntry[ShutMucr](movementCacheId(), ShutMucr.formId).map {
      case Some(data) => Ok(shutMucrSummaryPage(data))
      case None       => throw IncompleteApplication
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService.fetchAndGetEntry[ShutMucr](movementCacheId(), ShutMucr.formId).flatMap {
      case Some(shutMucr) =>
        submissionService
          .submitShutMucrRequest(shutMucr, request.eori)
          .map { _ =>
            Redirect(routes.ShutMucrConfirmationController.displayPage())
              .flashing(FlashKeys.MUCR -> shutMucr.mucr)
          }
          .andThen {
            case Success(_) => cacheService.remove(movementCacheId())
          }
      case None => throw IncompleteApplication
    }
  }
}
