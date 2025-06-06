/*
 * Copyright 2024 HM Revenue & Customs
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

package models.requests

import play.api.mvc.{Request, Session}

object SessionHelper {
  val ANSWER_CACHE_ID = "ANSWER_CACHE_ID"
  val CONVERSATION_ID = "CONVERSATION_ID"
  val JOURNEY_TYPE = "JOURNEY_TYPE"
  val MUCR = "MUCR"
  val UCR = "UCR"
  val UCR_TYPE = "UCR_TYPE"

  private val receiptPageSessionKeys = List(CONVERSATION_ID, JOURNEY_TYPE, MUCR, UCR, UCR_TYPE)

  def getValue(key: String)(implicit request: Request[_]): Option[String] =
    request.session.data.get(key)

  def removeValue(key: String)(implicit request: Request[_]): Session =
    request.session - key

  def clearAllReceiptPageSessionKeys()(implicit request: Request[_]): Session =
    request.session -- receiptPageSessionKeys
}
