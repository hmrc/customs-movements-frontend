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
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ducr_confirmation

@Singleton
class AssociateUcrConfirmationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  associateDucrConfirmPage: associate_ducr_confirmation
) extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(associateDucrConfirmPage())
  }
}
