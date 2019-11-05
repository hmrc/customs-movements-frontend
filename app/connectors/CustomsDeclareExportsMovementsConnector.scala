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

package connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.external.requests.ConsolidationRequest
import models.notifications.NotificationFrontendModel
import models.requests.MovementRequest
import models.submissions.ActionType._
import models.submissions.{ActionType, SubmissionFrontendModel}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  private val CustomsDeclareExportsMovementsUrl = s"${appConfig.customsDeclareExportsMovements}"

  private val movementSubmissionUrl: PartialFunction[ActionType, String] = {
    case Arrival | Departure => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementsSubmissionUri}"
  }

  private val JsonHeaders = Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

  def sendArrivalDeclaration(movementRequest: MovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    postRequest(Arrival, movementRequest)

  def sendDepartureDeclaration(movementRequest: MovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    postRequest(Departure, movementRequest)

  private def postRequest(
    actionType: ActionType,
    movementRequest: MovementRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient
      .POST[MovementRequest, HttpResponse](movementSubmissionUrl(actionType), movementRequest, JsonHeaders)
      .andThen {
        case Success(response) =>
          logger.debug(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS response on ${actionType.value}. $response")
        case Failure(exception) =>
          logger.warn(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS failure on ${actionType.value}. $exception ")
      }

  def sendConsolidationRequest(consolidation: ConsolidationRequest)(implicit hc: HeaderCarrier): Future[ConsolidationRequest] =
    httpClient.POST[ConsolidationRequest, ConsolidationRequest](
      s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationUri}",
      consolidation,
      JsonHeaders
    )

  def fetchNotifications(
    conversationId: String,
    eori: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[NotificationFrontendModel]] =
    httpClient
      .GET[Seq[NotificationFrontendModel]](
        s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchNotifications}/$conversationId",
        eoriQueryParam(eori)
      )
      .andThen {
        case Success(response)  => logger.debug(s"Notifications fetch response. $response")
        case Failure(exception) => logger.warn(s"Notifications fetch failure. $exception")
      }

  def fetchAllNotificationsForUser(eori: String)(implicit hc: HeaderCarrier): Future[Seq[NotificationFrontendModel]] =
    httpClient
      .GET[Seq[NotificationFrontendModel]](s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchNotifications}", eoriQueryParam(eori))
      .andThen {
        case Success(response)  => logger.debug(s"Notifications fetch response. $response")
        case Failure(exception) => logger.warn(s"Notifications fetch failure. $exception")
      }

  def fetchAllSubmissions(eori: String)(implicit hc: HeaderCarrier): Future[Seq[SubmissionFrontendModel]] =
    httpClient
      .GET[Seq[SubmissionFrontendModel]](s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchAllSubmissions}", eoriQueryParam(eori))
      .andThen {
        case Success(response)  => logger.debug(s"Submissions fetch response. $response")
        case Failure(exception) => logger.warn(s"Submissions fetch failure. $exception")
      }

  def fetchSingleSubmission(conversationId: String, eori: String)(implicit hc: HeaderCarrier): Future[Option[SubmissionFrontendModel]] =
    httpClient
      .GET[Option[SubmissionFrontendModel]](
        s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchSingleSubmission}/$conversationId",
        eoriQueryParam(eori)
      )
      .andThen {
        case Success(response)  => logger.debug(s"Single submission fetch response. $response")
        case Failure(exception) => logger.warn(s"Single submission fetch failure. $exception")
      }

  private def eoriQueryParam(eori: String): Seq[(String, String)] = Seq("eori" -> eori)
}
