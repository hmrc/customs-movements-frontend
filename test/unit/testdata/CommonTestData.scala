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

package testdata

object CommonTestData {

  val validEori: String = "GB12345678"

  val correctUcr: String = "GB/1UZYBD3XE-1J8MEBF9N6X65B"
  val correctUcr_2: String = "GB/1UZYBD3XE-1J8MEBF9N6X78C"
  val correctUcr_3: String = "GB/1UZYBD3XE-1J8MEBF9N6XABC"

  val validMucr = "GB/1234567890-MUCR"
  val validDucr = "4GB123456789000-DUCR"
  val validDucrPartId = "123m"
  val validWholeDucrParts = s"${validDucr}-${validDucrPartId}"

  val conversationId: String = "93feaae9-5043-4569-9fc5-ff04bfea0d11"
  val conversationId_2: String = "93feaae9-5043-4569-9fc5-ff04bfea0d22"
  val conversationId_3: String = "93feaae9-5043-4569-9fc5-ff04bfea0d33"
  val conversationId_4: String = "93feaae9-5043-4569-9fc5-ff04bfea0d44"
}
