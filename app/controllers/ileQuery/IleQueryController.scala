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

package controllers.ileQuery

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.{AuthAction, IleQueryAction}
import forms.IleQueryForm.form
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.{CacheRepository, IleQueryRepository}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html._

import scala.concurrent.ExecutionContext

@Singleton
class IleQueryController @Inject()(
  authenticate: AuthAction,
  ileQueryAction: IleQueryAction,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  cacheRepository: CacheRepository,
  ileQueryRepository: IleQueryRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  ileQueryPage: ile_query
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayQueryForm(): Action[AnyContent] = (authenticate andThen ileQueryAction) { implicit request =>
    Ok(ileQueryPage(form))
  }

  def submitQueryForm(): Action[AnyContent] = authenticate { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => BadRequest(ileQueryPage(formWithErrors)), validUcr => ???)
  }
}
