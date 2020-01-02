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

package connectors

import config.AppConfig
import connectors.exchanges.{Consolidation, Query, QueryResult}
import javax.inject.{Inject, Singleton}
import models.notifications.Notification
import models.requests.MovementRequest
import models.submissions.Submission
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  import CustomsDeclareExportsMovementsConnector._

  private val logger = Logger(this.getClass)

  private val JsonHeaders = Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

  def submit(request: MovementRequest)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .POST[MovementRequest, HttpResponse](appConfig.customsDeclareExportsMovements + Movements, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit Movement", response.body)
        case Failure(exception) => logFailedExchange("Submit Movement", exception)
      }
      .map(_ => (): Unit)

  def submit[T <: Consolidation](request: T)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .POST[T, HttpResponse](appConfig.customsDeclareExportsMovements + Consolidations, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit Consolidation", response.body)
        case Failure(exception) => logFailedExchange("Submit Consolidation", exception)
      }
      .map(_ => (): Unit)

  def submit(request: Query)(implicit hc: HeaderCarrier): Future[QueryResult] = ???

  def fetchAllSubmissions(eori: String)(implicit hc: HeaderCarrier): Future[Seq[Submission]] =
    httpClient
      .GET[Seq[Submission]](s"${appConfig.customsDeclareExportsMovements}$Submissions", eoriQueryParam(eori))

  def fetchSingleSubmission(conversationId: String, eori: String)(implicit hc: HeaderCarrier): Future[Option[Submission]] =
    httpClient
      .GET[Option[Submission]](s"${appConfig.customsDeclareExportsMovements}$Submissions/$conversationId", eoriQueryParam(eori))

  def fetchNotifications(conversationId: String, eori: String)(implicit hc: HeaderCarrier): Future[Seq[Notification]] =
    httpClient
      .GET[Seq[Notification]](s"${appConfig.customsDeclareExportsMovements}$Notifications/$conversationId", eoriQueryParam(eori))

  def fetchAllNotificationsForUser(eori: String)(implicit hc: HeaderCarrier): Future[Seq[Notification]] =
    httpClient
      .GET[Seq[Notification]](s"${appConfig.customsDeclareExportsMovements}$Notifications", eoriQueryParam(eori))

  private def eoriQueryParam(eori: String): Seq[(String, String)] = Seq("eori" -> eori)

  private def logSuccessfulExchange[T](`type`: String, payload: T)(implicit fmt: Format[T]): Unit =
    logger.debug(`type` + "\n" + Json.toJson(payload))

  private def logFailedExchange(`type`: String, exception: Throwable): Unit =
    logger.warn(`type` + " failed", exception)
}

object CustomsDeclareExportsMovementsConnector {
  val Movements = "/movements"
  val Consolidations = "/consolidation"
  val Submissions = "/submissions"
  val Notifications = "/notifications"
}
