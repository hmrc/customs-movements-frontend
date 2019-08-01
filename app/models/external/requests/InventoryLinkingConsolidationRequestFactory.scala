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

package models.external.requests

import scala.xml.{Node, NodeSeq}

object InventoryLinkingConsolidationRequestFactory {

  private val AssociationCode = "EAC"
  private val DisassociationCode = "EAC"
  private val ShutMucrCode = "CST"

  def buildAssociationRequest(mucr: String, ducr: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{AssociationCode}</messageCode>
        {buildMasterUcrNode(Some(mucr))}
        {buildUcrBlockNode(Some(ducr))}
      </inventoryLinkingConsolidationRequest>
    }

  def buildDisassociationRequest(ducr: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{DisassociationCode}</messageCode>
        {buildUcrBlockNode(Some(ducr))}
      </inventoryLinkingConsolidationRequest>
    }

  def buildShutMucrRequest(mucr: String): Node =
    scala.xml.Utility.trim {
      <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
        <messageCode>{ShutMucrCode}</messageCode>
        {buildMasterUcrNode(Some(mucr))}
      </inventoryLinkingConsolidationRequest>
    }

  private def buildMasterUcrNode(mucrOpt: Option[String]): NodeSeq =
    mucrOpt.map(mucr => <masterUCR>{mucr}</masterUCR>).getOrElse(NodeSeq.Empty)

  private def buildUcrBlockNode(ducrOpt: Option[String]): NodeSeq =
    ducrOpt.map { ducr =>
      <ucrBlock>
        <ucr>{ducr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    }.getOrElse(NodeSeq.Empty)

}
