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

package views.components.config

import config.IleQueryConfig
import controllers.actions.ArriveDepartAllowList
import controllers.ileQuery.routes.{FindConsignmentController, IleQueryController}
import controllers.routes.DucrPartDetailsController
import forms.UcrType.DucrPart
import models.UcrBlock
import play.api.mvc.Call

import javax.inject.Inject

class ChoicePageConfig @Inject() (ileQueryConfig: IleQueryConfig, arriveDepartAllowList: ArriveDepartAllowList) extends BaseConfig(ileQueryConfig) {

  def backLink(queryUcr: Option[UcrBlock]): Option[Call] =
    if (ileQueryConfig.isIleQueryEnabled)
      queryUcr.map { ucrBlock =>
        if (ucrBlock.is(DucrPart)) DucrPartDetailsController.displayPage()
        else IleQueryController.getConsignmentInformation(ucrBlock.ucr)
      }.orElse(Some(FindConsignmentController.displayQueryForm()))
    else
      None

  def isUserPermittedArriveDepartAccess(eori: String): Boolean =
    arriveDepartAllowList.contains(eori)
}
