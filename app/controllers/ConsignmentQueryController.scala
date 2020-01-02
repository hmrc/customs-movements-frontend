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

package controllers

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{ile_loading_result, ile_query}

import scala.concurrent.ExecutionContext

class ConsignmentQueryController @Inject()(
  authenticate: AuthAction,
  connector: CustomsDeclareExportsMovementsConnector,
  mcc: MessagesControllerComponents,
  ileQuery: ile_query,
  ileLoadingResult: ile_loading_result
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val mapping: Mapping[String] = Forms.mapping[String, String]("query" -> text())(identity)(Some(_))

  private val form: Form[String] = Form(mapping)

  var result = false

  def searchGet(): Action[AnyContent] = authenticate { implicit request =>
    Ok(ileQuery(form))
  }

  def search(): Action[AnyContent] = authenticate { implicit request =>

    form.bindFromRequest().fold(
      formWithErrors => Ok(ileQuery(formWithErrors)),
      validQuery =>
        // send query with value
        Redirect(routes.ConsignmentQueryController.get(validQuery))
    )
  }

  def get(id: String): Action[AnyContent] = authenticate { implicit request =>

    // Check if the result is, result var is just a mock for it

    if (result) {
      result = false
      Ok(ileLoadingResult("Result"))
    }
    else {
      result = true
      Ok(ileLoadingResult("Loading")).withHeaders("refresh" -> "5")
    }

  }

}
