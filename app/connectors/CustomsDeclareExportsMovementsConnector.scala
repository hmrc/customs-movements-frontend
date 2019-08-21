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
import models.NotificationContract
import models.submissions.SubmissionContract
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  private val logger = Logger(this.getClass)

  private val CustomsDeclareExportsMovementsUrl = s"${appConfig.customsDeclareExportsMovements}"
  private val ArrivalSubmissionUrl =
    s"$CustomsDeclareExportsMovementsUrl${appConfig.movementArrivalSubmissionUri}"
  private val DepartureSubmissionUrl =
    s"$CustomsDeclareExportsMovementsUrl${appConfig.movementDepartureSubmissionUri}"
  private val AssociateConsolidationUrl =
    s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationAssociateUri}"
  private val DisassociateConsolidationUrl =
    s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationDisassociateUri}"
  private val ShutMucrConsolidationUrl =
    s"$CustomsDeclareExportsMovementsUrl${appConfig.movementConsolidationShutMucrUri}"

  private val CommonMovementsHeaders =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8), HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8))

  def sendArrivalDeclaration(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(ArrivalSubmissionUrl, requestXml)

  def sendDepartureDeclaration(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(DepartureSubmissionUrl, requestXml)

  def sendAssociationRequest(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(AssociateConsolidationUrl, requestXml)

  def sendDisassociationRequest(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(DisassociateConsolidationUrl, requestXml)

  def sendShutMucrRequest(requestXml: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postRequest(ShutMucrConsolidationUrl, requestXml)

  private def postRequest(
    url: String,
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient
      .POSTString[HttpResponse](url, requestXml, CommonMovementsHeaders)
      .map(logResponse)

  def fetchNotifications(
    conversationId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[NotificationContract]] =
    httpClient.GET[Seq[NotificationContract]](
      s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchNotifications}/$conversationId"
    )

  def fetchSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SubmissionContract]] =
    httpClient
      .GET[Seq[SubmissionContract]](s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchMovements}")
      .map { response =>
        logger.debug(s"CUSTOMS_MOVEMENTS_FRONTEND fetch submission response is --> ${response.toString}")
        response
      }

  private def logResponse(response: HttpResponse): HttpResponse = {
    logger.debug(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS response is --> ${response.toString}")
    response
  }

}
