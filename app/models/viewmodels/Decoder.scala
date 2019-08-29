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

package models.viewmodels

import javax.inject.Singleton

@Singleton
class Decoder {

  def crc(code: String): String = code match {
    case "000" => "decoder.crc.Success"
    case "101" => "decoder.crc.PrelodgedDeclarationNotArrived"
    case "102" => "decoder.crc.DeclarationNotArrived"
    case _     => ""
  }

  def roe(code: String): String = code match {
    case "1" => "decoder.roe.DocumentaryControl"
    case "2" => "decoder.roe.PhysicalExternalPartyControl"
    case "3" => "decoder.roe.NonBlockingDocumentaryControl"
    case "6" => "decoder.roe.NoControlRequired"
    case "0" => "decoder.roe.RiskingNotPerformed"
    case "H" => "decoder.roe.PrelodgePrefix"
    case _   => ""
  }

  def soe(code: String): String = code match {
    case "1"  => "decoder.soe.DeclarationValidation"
    case "2"  => "decoder.soe.DeclarationGoodsRelease"
    case "3"  => "decoder.soe.DeclarationClearance"
    case "4"  => "decoder.soe.DeclarationInvalidated"
    case "5"  => "decoder.soe.DeclarationRejected"
    case "6"  => "decoder.soe.DeclarationHandledExternally"
    case "7"  => "decoder.soe.DeclarationCorrectionValidation"
    case "8"  => "decoder.soe.AdvanceDeclarationRegistration"
    case "9"  => "decoder.soe.DeclarationAcceptance"
    case "10" => "decoder.soe.DeclarationAcceptanceAtGoodsArrival"
    case "11" => "decoder.soe.DeclarationRejectionAtGoodsArrival"
    case "12" => "decoder.soe.DeclarationCorrected"
    case "13" => "decoder.soe.DeclarationSupplemented"
    case "14" => "decoder.soe.DeclarationRisked"
    case "15" => "decoder.soe.CustomsPositionDetermined"
    case "16" => "decoder.soe.DeclarationClearanceAfterGoodsRelease"
    case "17" => "decoder.soe.InsufficientGuarantees"
    case "D"  => "decoder.soe.Departed"
    case "F"  => "decoder.soe.Frustrated"
    case _    => ""
  }

  def actionCode(code: String): String = code match {
    case "1" => "notifications.elem.content.inventoryLinkingControlResponse.AcknowledgedAndProcessed"
    case "2" => "notifications.elem.content.inventoryLinkingControlResponse.PartiallyAcknowledgedAndProcessed"
    case "3" => "notifications.elem.content.inventoryLinkingControlResponse.Rejected"
    case _ => ""
  }
}
