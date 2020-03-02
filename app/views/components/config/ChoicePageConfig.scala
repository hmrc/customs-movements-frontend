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

import config.AppConfig
import javax.inject.Inject
import models.UcrBlock
import play.api.mvc.Call

class ChoicePageConfig @Inject()(appConfig: AppConfig) {
  def backLink(queryUcr: Option[UcrBlock]): Call =
    if (appConfig.ileQueryEnabled)
      queryUcr
        .map(block => controllers.ileQuery.routes.IleQueryController.getConsignmentInformation(block.ucr))
        .getOrElse(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
    else
      controllers.routes.StartController.displayStartPage

  def isQueryEnabled: Boolean = appConfig.ileQueryEnabled
}
