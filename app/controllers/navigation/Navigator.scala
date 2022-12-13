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

package controllers.navigation

import controllers.consolidations.routes.{ArriveOrDepartSummaryController, AssociateUcrSummaryController}
import forms.{FormAction, SaveAndReturnToSummary}
import models.cache.JourneyType
import models.requests.RequestWithAnswers
import play.api.mvc.Results.Redirect
import play.api.mvc._

class Navigator {

  def continueTo(redirectTo: Call)(implicit request: RequestWithAnswers[AnyContent]): Result =
    (FormAction.bindFromRequest, request.answers.`type`) match {
      case (Some(SaveAndReturnToSummary), JourneyType.ASSOCIATE_UCR) => Redirect(AssociateUcrSummaryController.displayPage)
      case (Some(SaveAndReturnToSummary), _)                         => Redirect(ArriveOrDepartSummaryController.displayPage)
      case _                                                         => Redirect(redirectTo)
    }
}
