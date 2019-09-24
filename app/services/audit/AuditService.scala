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

package services.audit

import com.google.inject.Inject
import forms._
import javax.inject.Named
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(connector: AuditConnector, @Named("appName") appName: String)(
  implicit ec: ExecutionContext
) {
  private val logger = Logger(this.getClass)

  def auditShutMucr(eori: String, mucr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditTypes.AuditShutMucr,
      Map(
        EventData.EORI.toString -> eori,
        EventData.MUCR.toString -> mucr,
        EventData.SubmissionResult.toString -> result
      )
    )

  def auditDisassociate(eori: String, ducr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditTypes.AuditDisassociate,
      Map(
        EventData.EORI.toString -> eori,
        EventData.DUCR.toString -> ducr,
        EventData.SubmissionResult.toString -> result
      )
    )

  def auditAssociate(eori: String, mucr: String, ducr: String, result: String)(
    implicit hc: HeaderCarrier
  ): Future[AuditResult] =
    audit(
      AuditTypes.AuditAssociate,
      Map(
        EventData.EORI.toString -> eori,
        EventData.MUCR.toString -> mucr,
        EventData.DUCR.toString -> ducr,
        EventData.SubmissionResult.toString -> result
      )
    )

  def audit(auditType: AuditTypes.Audit, auditData: Map[String, String])(
    implicit hc: HeaderCarrier
  ): Future[AuditResult] = {
    val event = createAuditEvent(auditType, auditData)
    connector.sendEvent(event).map(handleResponse(_, auditType.toString))
  }

  private def createAuditEvent(choice: AuditTypes.Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier) =
    DataEvent(
      auditSource = appName,
      auditType = choice.toString,
      tags = getAuditTags(s"${choice}-request", path = s"$choice.value}"),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails() ++ auditData
    )

  private def getAuditTags(transactionName: String, path: String)(implicit hc: HeaderCarrier) =
    AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"Export-Declaration-${transactionName}",
        path = s"customs-declare-exports/${path}"
      )

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

  def auditMovements(
    eori: String,
    data: InventoryLinkingMovementRequest,
    result: String,
    movementAuditType: AuditTypes.Audit
  )(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      movementAuditType,
      Map(
        EventData.EORI.toString -> eori,
        EventData.MessageCode.toString -> data.messageCode,
        EventData.UCRType.toString -> data.ucrBlock.ucrType,
        EventData.UCR.toString -> data.ucrBlock.ucr,
        EventData.MovementReference.toString -> data.movementReference.getOrElse(""),
        EventData.SubmissionResult.toString -> result
      )
    )

  def auditAllPagesUserInput(choice: Choice, cacheMap: CacheMap)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditType = choice.value
    val extendedEvent = ExtendedDataEvent(
      auditSource = appName,
      auditType = auditType,
      tags = getAuditTags(s"${auditType}-payload-request", s"${auditType}/full-payload"),
      detail = getAuditDetails(getMovementsData(choice, cacheMap))
    )
    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, auditType))
  }

  def getMovementsData(choice: Choice, cacheMap: CacheMap): JsObject = {
    val movementDetails =
      if (choice.value == Choice.AllowedChoiceValues.Arrival)
        Json.toJson(cacheMap.getEntry[ArrivalDetails](MovementDetails.formId))
      else Json.toJson(cacheMap.getEntry[DepartureDetails](MovementDetails.formId))

    val userInput = Map(
      ConsignmentReferences.formId -> Json.toJson(
        cacheMap.getEntry[ConsignmentReferences](ConsignmentReferences.formId)
      ),
      GoodsDeparted.formId -> Json.toJson(cacheMap.getEntry[GoodsDeparted](GoodsDeparted.formId)),
      Location.formId -> Json.toJson(cacheMap.getEntry[Location](Location.formId)),
      MovementDetails.formId -> movementDetails,
      Transport.formId -> Json.toJson(cacheMap.getEntry[Transport](Transport.formId)),
      ArrivalReference.formId -> Json.toJson(cacheMap.getEntry[ArrivalReference](ArrivalReference.formId))
    )
    Json.toJson(userInput).as[JsObject]
  }

  private def getAuditDetails(userInput: JsObject)(implicit hc: HeaderCarrier) = {
    val hcAuditDetails = Json.toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()).as[JsObject]
    hcAuditDetails.deepMerge(userInput)
  }
}

object AuditTypes extends Enumeration {
  type Audit = Value
  val AuditArrival: AuditTypes.Value = Value("Arrival")
  val AuditDeparture: AuditTypes.Value = Value("Departure")
  val AuditAssociate: AuditTypes.Value = Value("Associate")
  val AuditDisassociate: AuditTypes.Value = Value("Disassociate")
  val AuditShutMucr: AuditTypes.Value = Value("ShutMucr")
}
object EventData extends Enumeration {
  type Data = Value
  val EORI, MUCR, DUCR, UCR, UCRType, MessageCode, MovementReference, SubmissionResult, Success, Failure = Value
}
