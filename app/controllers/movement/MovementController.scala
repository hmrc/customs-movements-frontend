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

package controllers.movement

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.{eoriCacheId, movementCacheId}
import forms.MovementFormsAndIds._
import forms._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.movement._

import scala.concurrent.{ExecutionContext, Future}

class MovementController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayGoodsDate(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      customsCacheService
        .fetchAndGetEntry[Choice](eoriCacheId, Choice.choiceId)
        .flatMap {
          case Some(choice) if !choice.value.isEmpty =>
            customsCacheService
              .fetchAndGetEntry[GoodsDateForm](movementCacheId, goodsDateId)
              .map {
                case Some(data) =>
                  Ok(goods_date(appConfig, goodsDateForm.fill(data), choice.value))
                case _ => Ok(goods_date(appConfig, goodsDateForm, choice.value))
              }
          case _ =>
            Future.successful(
              BadRequest(
                errorHandler.standardErrorTemplate(
                  pageTitle = messagesApi("global.error.title"),
                  heading = messagesApi("global.error.heading"),
                  message = messagesApi("global.error.message")
                )
              )
            )
        }
    }

  def saveGoodsDate(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      goodsDateForm
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[GoodsDateForm]) =>
            Future.successful(BadRequest(goods_date(appConfig, formWithErrors, "error"))),
          form =>
            customsCacheService
              .cache[GoodsDateForm](movementCacheId, goodsDateId, form)
              .map { _ =>
                Redirect(
                  controllers.routes.LocationController
                    .displayPage()
                )
            }
        )
    }

  def displayTransport(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      customsCacheService
        .fetchAndGetEntry[TransportForm](movementCacheId, transportId)
        .map {
          case Some(data) => Ok(transport(appConfig, transportForm.fill(data)))
          case _          => Ok(transport(appConfig, transportForm))
        }
    }

  def saveTransport(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      transportForm
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[TransportForm]) => Future.successful(BadRequest(transport(appConfig, formWithErrors))),
          form =>
            customsCacheService
              .cache[TransportForm](movementCacheId, transportId, form)
              .map { _ =>
                Redirect(
                  controllers.movement.routes.MovementSummaryController
                    .displaySummary()
                )
            }
        )
    }
}
