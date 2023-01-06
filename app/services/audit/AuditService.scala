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

package services.audit

import com.google.inject.Inject
import connectors.exchanges.MovementRequest
import forms._
import models.cache.{Answers, ArrivalAnswers, DepartureAnswers, JourneyType}
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, Json}
import services.audit.AuditService.EventData
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (connector: AuditConnector, @Named("appName") appName: String)(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)

  def auditShutMucr(eori: String, mucr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditType.AuditShutMucr,
      Map(EventData.eori.toString -> eori, EventData.mucr.toString -> mucr, EventData.submissionResult.toString -> result)
    )

  def auditDisassociate(eori: String, ucr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditType.AuditDisassociate,
      Map(EventData.eori.toString -> eori, EventData.ucr.toString -> ucr, EventData.submissionResult.toString -> result)
    )

  def auditAssociate(eori: String, mucr: String, ducr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditType.AuditAssociate,
      Map(
        EventData.eori.toString -> eori,
        EventData.mucr.toString -> mucr,
        EventData.ducr.toString -> ducr,
        EventData.submissionResult.toString -> result
      )
    )

  def auditMovements(data: MovementRequest, result: String, movementAuditType: AuditType.Audit)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      movementAuditType,
      Map(
        EventData.eori.toString -> data.eori,
        EventData.messageCode.toString -> data.choice.ileCode,
        EventData.ucrType.toString -> data.consignmentReference.reference,
        EventData.ucr.toString -> data.consignmentReference.referenceValue,
        EventData.submissionResult.toString -> result
      )
    )

  def audit(auditType: AuditType.Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val event = createAuditEvent(auditType, auditData)
    connector.sendEvent(event).map(handleResponse(_, auditType.toString))
  }

  private def createAuditEvent(auditType: AuditType.Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier) =
    DataEvent(
      auditSource = appName,
      auditType = auditType.toString,
      tags = getAuditTags(s"${auditType}-request", path = s"$auditType"),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails() ++ auditData
    )

  private def getAuditTags(transactionName: String, path: String)(implicit hc: HeaderCarrier) =
    AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(transactionName = s"export-declaration-${transactionName}", path = s"customs-declare-exports/${path}")

  private def handleResponse(result: AuditResult, auditType: String) = result match {
    case Success =>
      logger.debug(s"Exports ${auditType} audit successful")
      Success
    case Failure(err, _) =>
      logger.warn(s"Exports ${auditType} Audit Error, message: $err")
      Failure(err)
    case Disabled =>
      logger.warn(s"Auditing Disabled")
      Disabled
  }

  def auditAllPagesUserInput(eori: String, answers: Answers)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditType =
      if (answers.`type` == JourneyType.ARRIVE)
        AuditType.AuditArrival.toString
      else AuditType.AuditDeparture.toString

    val extendedEvent = ExtendedDataEvent(
      auditSource = appName,
      auditType = auditType,
      tags = getAuditTags(s"${auditType}-payload-request", s"${auditType}/full-payload"),
      detail = getAuditDetails(getMovementsData(eori, answers))
    )
    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, auditType))
  }

  private def getMovementsData(eori: String, answers: Answers): JsObject = {

    val userInput = (answers: @unchecked) match {
      case arrivalAnswers: ArrivalAnswers =>
        Map(
          EventData.eori.toString -> JsString(eori),
          ConsignmentReferences.formId -> Json.toJson(arrivalAnswers.consignmentReferences),
          Location.formId -> Json.toJson(arrivalAnswers.location),
          MovementDetails.formId -> Json.toJson(arrivalAnswers.arrivalDetails)
        )
      case departureAnswers: DepartureAnswers =>
        Map(
          EventData.eori.toString -> JsString(eori),
          ConsignmentReferences.formId -> Json.toJson(departureAnswers.consignmentReferences),
          Location.formId -> Json.toJson(departureAnswers.location),
          MovementDetails.formId -> Json.toJson(departureAnswers.departureDetails),
          Transport.formId -> Json.toJson(departureAnswers.transport)
        )
    }

    Json.toJson(userInput).as[JsObject]
  }

  private def getAuditDetails(userInput: JsObject)(implicit hc: HeaderCarrier) = {
    val hcAuditDetails = Json.toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()).as[JsObject]
    hcAuditDetails.deepMerge(userInput)
  }
}

object AuditService {

  object EventData extends Enumeration {
    type Data = Value

    val eori, mucr, ducr, ucr, ucrType, messageCode, submissionResult, Success, Failure = Value
  }
}
