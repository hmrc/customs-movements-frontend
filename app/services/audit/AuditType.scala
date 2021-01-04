/*
 * Copyright 2021 HM Revenue & Customs
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

object AuditType extends Enumeration {
  type Audit = Value

  val AuditArrival: AuditType.Value = Value("arrival")
  val AuditDeparture: AuditType.Value = Value("departure")
  val AuditAssociate: AuditType.Value = Value("associate")
  val AuditDisassociate: AuditType.Value = Value("disassociate")
  val AuditShutMucr: AuditType.Value = Value("shut-mucr")
}
