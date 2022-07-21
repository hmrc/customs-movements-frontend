/*
 * Copyright 2022 HM Revenue & Customs
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

package views.components.config

import base.UnitSpec
import forms.UcrType.{Ducr, DucrPart}
import models.UcrBlock
import testdata.CommonTestData.validWholeDucrParts

class ChoicePageConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  private val queryUcr = UcrBlock(ucrType = Ducr.codeValue, ucr = "ucr")

  "ChoicePageConfig on backLink" when {

    "ileQuery disabled" should {

      val config = new ChoicePageConfig(ileQueryDisabled, ducrPartDisabled, eoriInArriveDepartAllowList)

      "return correct url" in {

        config.backLink(None) mustBe None
        config.backLink(Some(queryUcr)) mustBe None
      }

      "return information about ile query" in {

        config.ileQueryEnabled mustBe false
      }
    }

    "ileQuery enabled" when {

      "ducrPart disabled" should {

        val config = new ChoicePageConfig(ileQueryEnabled, ducrPartDisabled, eoriInArriveDepartAllowList)

        "return correct url when query ucr present" in {

          config.backLink(Some(queryUcr)) mustBe Some(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("ucr"))
        }

        "return correct url when query ucr not present" in {

          config.backLink(None) mustBe Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
        }

        "return information about ile query" in {

          config.ileQueryEnabled mustBe true
        }
      }

      "ducrPart enabled" when {

        val config = new ChoicePageConfig(ileQueryEnabled, ducrPartEnabled, eoriInArriveDepartAllowList)

        "query ucr is not present" should {
          "return url to 'Find Consignment' page" in {

            config.backLink(None) mustBe Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
          }
        }

        "query ucr is present" when {

          "it is not DUCR Part" should {
            "return url to 'ILE Query' page" in {

              config.backLink(Some(queryUcr)) mustBe Some(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("ucr"))
            }
          }

          "it is DUCR Part" should {
            "return url to 'DUCR Part Details' page" in {

              val ducrPartQueryUcr = UcrBlock(ucrType = DucrPart.codeValue, ucr = validWholeDucrParts)

              config.backLink(Some(ducrPartQueryUcr)) mustBe Some(controllers.routes.DucrPartDetailsController.displayPage())
            }
          }
        }
      }
    }
  }
}
