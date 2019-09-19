package services.audit
import base.BaseSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import services.audit.EventData._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.wco.dec.inventorylinking.common.UcrBlock
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends BaseSpec {

  implicit val mockExecutionContext = mock[ExecutionContext]

  val mockConnector = mock[AuditConnector]
  val auditService = new AuditService(mockConnector, "appName")

  "AuditService" should {
    "audit Shut a Mucr data" in {
      auditService.auditShutMucrData("eori", "mucr", "200") mustBe Map(
        EORI.toString -> "eori",
        MUCR.toString -> "mucr",
        SubmissionResult.toString -> "200"
      )
    }

    "audit an association" in {
      auditService.auditAssociateData("eori", "mucr", "ducr", "200") mustBe Map(
        EORI.toString -> "eori",
        MUCR.toString -> "mucr",
        DUCR.toString -> "ducr",
        SubmissionResult.toString -> "200"
      )
    }

    "audit a disassociation" in {
      auditService.auditDisassociateData("eori", "ducr", "200") mustBe Map(
        EORI.toString -> "eori",
        DUCR.toString -> "ducr",
        SubmissionResult.toString -> "200"
      )
    }

    "audit a movement" in {
      val data =
        InventoryLinkingMovementRequest(messageCode = "EAD", ucrBlock = UcrBlock("UCR", "D"), goodsLocation = "")
      auditService.auditMovementsData("eori", data, "200") mustBe Map(
        "MovementReference" -> "",
        EORI.toString -> "eori",
        MessageCode.toString -> "EAD",
        UCR.toString -> "UCR",
        UCRType.toString -> "D",
        SubmissionResult.toString -> "200"
      )
    }
  }
}
