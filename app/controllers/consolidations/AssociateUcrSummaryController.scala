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
import forms.{AssociateUcr, MucrOptions}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{CustomsCacheService, SubmissionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ducr_summary

import scala.concurrent.ExecutionContext

@Singleton
class AssociateUcrSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  cacheService: CustomsCacheService,
  submissionService: SubmissionService,
  associateDucrSummaryPage: associate_ducr_summary
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    for {
      mucrOptions <- cacheService
        .fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId)
        .map(_.getOrElse(throw IncompleteApplication))

      associateUcr <- cacheService
        .fetchAndGetEntry[AssociateUcr](movementCacheId(), AssociateUcr.formId)
        .map(_.getOrElse(throw IncompleteApplication))
    } yield Ok(associateDucrSummaryPage(associateUcr, mucrOptions.mucr))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    for {
      mucrOptions <- cacheService
        .fetchAndGetEntry[MucrOptions](movementCacheId(), MucrOptions.formId)
        .map(_.getOrElse(throw IncompleteApplication))

      associateUcr <- cacheService
        .fetchAndGetEntry[AssociateUcr](movementCacheId(), AssociateUcr.formId)
        .map(_.getOrElse(throw IncompleteApplication))

      _ <- submissionService
        .submitUcrAssociation(mucrOptions, associateUcr, request.authenticatedRequest.user.eori)
      _ <- cacheService.remove(movementCacheId())

    } yield
      Redirect(routes.AssociateUcrConfirmationController.displayPage())
        .flashing(FlashKeys.UCR -> associateUcr.ucr, FlashKeys.CONSOLIDATION_KIND -> associateUcr.kind.formValue)
  }
}