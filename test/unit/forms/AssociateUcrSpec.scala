/*
 * Copyright 2020 HM Revenue & Customs
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

package forms

import base.BaseSpec
import models.UcrBlock

class AssociateUcrSpec extends BaseSpec {

  "AssociateUcr" should {

    "apply UcrBlock for Mucr" in {

      AssociateUcr.apply(UcrBlock("ucr", UcrType.Mucr)) mustBe AssociateUcr(UcrType.Mucr, "ucr")
    }

    "apply UcrBlock for Ducr" in {

      AssociateUcr.apply(UcrBlock("ucr", UcrType.Ducr)) mustBe AssociateUcr(UcrType.Ducr, "ucr")
    }
  }

}
