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

import config.IleQueryConfig
import javax.inject.Inject
import play.api.mvc.Call

class DucrPartDetailsConfig @Inject()(ileQueryConfig: IleQueryConfig) {

  def backUrl: Call =
    if (ileQueryConfig.isIleQueryEnabled)
      controllers.ileQuery.routes.FindConsignmentController.displayQueryForm()
    else
      controllers.routes.DucrPartChiefController.displayPage()

  def action: Call =
    if (ileQueryConfig.isIleQueryEnabled)
      controllers.routes.DucrPartDetailsController.submitDucrPartDetails()
    else
      controllers.routes.DucrPartDetailsController.submitDucrPartDetailsJourney()
}
