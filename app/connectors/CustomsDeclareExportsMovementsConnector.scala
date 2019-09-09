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
import models.notifications.NotificationFrontendModel
import models.submissions.ActionType._
import models.submissions.{ActionType, SubmissionFrontendModel}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  private val logger = Logger(this.getClass)

  private val CustomsDeclareExportsMovementsUrl = s"${appConfig.customsDeclareExportsMovements}"

  private val movementSubmissionUrl: PartialFunction[ActionType, String] = {
    case Arrival            => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementArrivalSubmissionUri}"
    case Departure          => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementDepartureSubmissionUri}"
    case DucrAssociation    => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationAssociateUri}"
    case DucrDisassociation => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationDisassociateUri}"
    case ShutMucr           => s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationShutMucrUri}"
  }

  private val CommonMovementsHeaders =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8), HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8))

  def sendArrivalDeclaration(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = postRequest(Arrival, requestXml)

  def sendDepartureDeclaration(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = postRequest(Departure, requestXml)

  def sendAssociationRequest(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = postRequest(DucrAssociation, requestXml)

  def sendDisassociationRequest(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(DucrDisassociation, requestXml)

  def sendShutMucrRequest(requestXml: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(ShutMucr, requestXml)

  private def postRequest(
    actionType: ActionType,
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient
      .POSTString[HttpResponse](movementSubmissionUrl(actionType), requestXml, CommonMovementsHeaders)
      .andThen {
        case Success(response) =>
          logger.debug(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS response on ${actionType.value}. $response")
        case Failure(exception) =>
          logger.warn(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS failure on ${actionType.value}. $exception ")
      }

  def fetchNotifications(
    conversationId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[NotificationFrontendModel]] =
    httpClient
      .GET[Seq[NotificationFrontendModel]](
        s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchNotifications}/$conversationId"
      )
      .andThen {
        case Success(response)  => logger.debug(s"Notifications fetch response. $response")
        case Failure(exception) => logger.warn(s"Notifications fetch failure. $exception")
      }

  def fetchAllSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SubmissionFrontendModel]] =
    httpClient
      .GET[Seq[SubmissionFrontendModel]](s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchAllSubmissions}")
      .andThen {
        case Success(response)  => logger.debug(s"Submissions fetch response. $response")
        case Failure(exception) => logger.warn(s"Submissions fetch failure. $exception")
      }

  def fetchSingleSubmission(
    conversationId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SubmissionFrontendModel]] =
    httpClient
      .GET[Option[SubmissionFrontendModel]](
        s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchSingleSubmission}/$conversationId"
      )
      .andThen {
        case Success(response)  => logger.debug(s"Single submission fetch response. $response")
        case Failure(exception) => logger.warn(s"Single submission fetch failure. $exception")
      }

}
