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

package controllers

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.Query
import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class ConsignmentQueryController @Inject()(
  authenticate: AuthAction,
  connector: CustomsDeclareExportsMovementsConnector,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val mapping: Mapping[String] = Forms.mapping[String, String]("query" -> text())(identity)(Some(_))

  private val form: Form[String] = Form(mapping)

  def search(query: Option[String]): Action[AnyContent] = authenticate.async { implicit request =>
    query match {
      case None    => Future.successful(Ok("What do you want to search for"))
      case Some(ducrOrMucr) => connector.submit(Query(ducrOrMucr, request.eori)).map { response =>
        Redirect(routes.ConsignmentQueryController.get(response.id))
      }
    }
  }

  def get(id: String): Action[AnyContent] = authenticate { implicit request =>
    Ok("Loading")
    // Will be extended to call the back end by queryId
  }

}
