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
import javax.inject.Inject
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.confirmation_link

class ConfirmationPageConfig @Inject()(ileQueryConfig: IleQueryConfig, confirmationLink: confirmation_link) {

  def nextStepLink()(implicit messages: Messages): Html =
    if (ileQueryConfig.isIleQueryEnabled) {
      confirmationLink(
        message = messages("confirmation.redirect.query.link"),
        linkTarget = controllers.ileQuery.routes.FindConsignmentController.displayQueryForm()
      )
    } else {
      confirmationLink(message = messages("confirmation.redirect.choice.link"), linkTarget = controllers.routes.ChoiceController.displayChoiceForm())
    }
}
