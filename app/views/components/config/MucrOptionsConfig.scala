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

package views.components.config

import config.{AppConfig, IleQueryConfig}
import forms.ManageMucrChoice
import javax.inject.Inject
import models.cache.DucrPartChiefAnswers
import models.requests.JourneyRequest
import play.api.mvc.Call

class MucrOptionsConfig @Inject()(appConfig: AppConfig, ileQueryConfig: IleQueryConfig) extends BaseConfig(ileQueryConfig) {

  def backUrl(manageMucrChoice: Option[ManageMucrChoice] = None)(implicit request: JourneyRequest[_]): Call =
    if (ileQueryConfig.isIleQueryEnabled && manageMucrChoice.isDefined)
      controllers.consolidations.routes.ManageMucrController.displayPage()
    else if (request.answersAs[DucrPartChiefAnswers].ducrPartChiefChoice.exists(_.isDucrPart))
      controllers.routes.DucrPartDetailsController.displayPage()
    else if (request.answersAs[DucrPartChiefAnswers].ducrPartChiefChoice.isDefined)
      controllers.routes.DucrPartChiefController.displayPage()
    else
      controllers.routes.ChoiceController.displayChoiceForm()

  def tradeTariffUrl: String = appConfig.tradeTariffUrl
}
