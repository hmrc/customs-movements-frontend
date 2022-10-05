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

package controllers.ileQuery

import controllers.actions.{AuthAction, IleQueryAction}
import controllers.ileQuery.routes.IleQueryController
import forms.IleQueryForm.form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ile_query

import javax.inject.{Inject, Singleton}

@Singleton
class FindConsignmentController @Inject() (
  authenticate: AuthAction,
  ileQueryFeatureEnabled: IleQueryAction,
  mcc: MessagesControllerComponents,
  ileQueryPage: ile_query
) extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen ileQueryFeatureEnabled) { implicit request =>
    Ok(ileQueryPage(form))
  }

  val submitPage: Action[AnyContent] = (authenticate andThen ileQueryFeatureEnabled) { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => BadRequest(ileQueryPage(formWithErrors)), validUcr => Redirect(IleQueryController.getConsignmentData(validUcr)))
  }
}
