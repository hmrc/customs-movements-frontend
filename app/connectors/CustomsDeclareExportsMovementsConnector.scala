/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.exception.MovementsConnectorException
import connectors.exchanges.{Consolidation, IleQueryExchange, MovementRequest}
import javax.inject.{Inject, Singleton}
import models.notifications.Notification
import models.submissions.Submission
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject() (appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  import CustomsDeclareExportsMovementsConnector._

  private val logger = Logger(this.getClass)

  private val JsonHeaders = Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

  def submit(request: MovementRequest)(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .POST[MovementRequest, HttpResponse](appConfig.customsDeclareExportsMovements + Movements, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit Movement", response.body)
        case Failure(exception) => logFailedExchange("Submit Movement", exception)
      }
      .map(response => handleResponse(response, response.body))

  def submit(request: Consolidation)(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .POST[Consolidation, HttpResponse](appConfig.customsDeclareExportsMovements + consolidation, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit Consolidation", response.body)
        case Failure(exception) => logFailedExchange("Submit Consolidation", exception)
      }
      .map(response => handleResponse(response, response.body))

  def submit(request: IleQueryExchange)(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .POST[IleQueryExchange, HttpResponse](appConfig.customsDeclareExportsMovements + appConfig.ileQueryUri, request, JsonHeaders)
      .andThen {
        case Success(response)  => logSuccessfulExchange("Submit ILE Query", response.body)
        case Failure(exception) => logFailedExchange("Submit ILE Query", exception)
      }
      .map(response => handleResponse(response, response.body))

  def fetchAllSubmissions(eori: String)(implicit hc: HeaderCarrier): Future[Seq[Submission]] =
    httpClient
      .GET[Seq[Submission]](s"${appConfig.customsDeclareExportsMovements}$Submissions", eoriQueryParam(eori))

  def fetchSingleSubmission(conversationId: String, eori: String)(implicit hc: HeaderCarrier): Future[Option[Submission]] =
    httpClient
      .GET[Option[Submission]](s"${appConfig.customsDeclareExportsMovements}$Submissions/$conversationId", eoriQueryParam(eori))

  def fetchNotifications(conversationId: String, eori: String)(implicit hc: HeaderCarrier): Future[Seq[Notification]] =
    httpClient
      .GET[Seq[Notification]](s"${appConfig.customsDeclareExportsMovements}$Notifications/$conversationId", eoriQueryParam(eori))

  def fetchQueryNotifications(conversationId: String, eori: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .GET[HttpResponse](s"${appConfig.customsDeclareExportsMovements}$IleQuery/$conversationId", eoriQueryParam(eori))
      .andThen {
        case Success(response)  => logSuccessfulExchange("Ile query response fetch", response.body)
        case Failure(exception) => logFailedExchange("Ile query response fetch", exception)
      }

  private def eoriQueryParam(eori: String): Seq[(String, String)] = Seq("eori" -> eori)

  private def logSuccessfulExchange[T](`type`: String, payload: T)(implicit fmt: Format[T]): Unit =
    logger.debug(`type` + "\n" + Json.toJson(payload))

  private def logFailedExchange(`type`: String, exception: Throwable): Unit =
    logger.warn(`type` + " failed", exception)

  private def handleResponse[T](response: HttpResponse, value: T): T =
    response.status match {
      case Status.ACCEPTED => value
      case _               => throw new MovementsConnectorException(s"Failed with response $response")
    }
}

object CustomsDeclareExportsMovementsConnector {
  val Movements = "/movements"
  val consolidation = "/consolidation"
  val Submissions = "/submissions"
  val Notifications = "/notifications"
  val IleQuery = "/consignment-query"
}
