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
import models.{Movement, NotificationPresentation}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.CustomsHeaderNames

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  private val logger = Logger(this.getClass)

  private val MovementSubmissionUrl = s"${appConfig.customsDeclareExportsMovements}${appConfig.saveMovementSubmission}"
  private val MovementConsolidationUrl =
    s"${appConfig.customsDeclareExportsMovements}${appConfig.submitMovementConsolidation}"

  private val CommonMovementsHeaders =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8), HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8))

  def submitMovementDeclaration(ucr: String, movementType: String, xmlBody: String)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .POSTString[HttpResponse](MovementSubmissionUrl, xmlBody, movementSubmissionHeaders(ucr, movementType))
      .map(logResponse)

  private def movementSubmissionHeaders(ucr: String, movementType: String): Seq[(String, String)] =
    CommonMovementsHeaders ++ Seq(
      CustomsHeaderNames.XUcr -> ucr,
      CustomsHeaderNames.XMovementType -> movementType.toString
    )

  def fetchNotifications(
    conversationId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[NotificationPresentation]] =
    httpClient.GET[Seq[NotificationPresentation]](
      s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchNotifications}/$conversationId"
    )

  def fetchSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Movement]] =
    httpClient.GET[Seq[Movement]](s"${appConfig.customsDeclareExportsMovements}${appConfig.fetchMovements}").map {
      response =>
        logger.debug(s"CUSTOMS_MOVEMENTS_FRONTEND fetch submission response is --> ${response.toString}")
        response
    }

  def sendConsolidationRequest(
    requestXml: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient
      .POSTString[HttpResponse](MovementConsolidationUrl, requestXml, CommonMovementsHeaders)
      .map(logResponse)

  private def logResponse(response: HttpResponse): HttpResponse = {
    logger.debug(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS response is --> ${response.toString}")
    response
  }

}
