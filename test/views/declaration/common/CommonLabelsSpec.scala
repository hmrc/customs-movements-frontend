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

package views.declaration.common
import helpers.views.CommonMessages
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class CommonLabelsSpec extends ViewSpec with CommonMessages {

  "Button labels" should {

    "have proper value for \"Back\" button" in {
      assertMessage(backCaption, "Back")
    }
  }

  "Global error labels" should {

    "have proper message for global error title" in {
      assertMessage(globalErrorTitle, "There is a problem - Declare customs exports for customs exports - GOV.UK")
    }

    "have proper message for global error heading" in {
      assertMessage(globalErrorHeading, "There is a problem with a service")
    }

    "have proper message for global error message" in {
      assertMessage(globalErrorMessage, "Please try again later.")
    }
  }

  "Error labels" should {

    "have proper message for error summary title" in {
      assertMessage(errorSummaryTitle, "There’s been a problem")
    }

    "have proper message for error summary text" in {
      assertMessage(errorSummaryText, "Check the following")
    }
  }

  "DUCR error labels" should {

    "have proper message for incorrect DUCR" in {
      assertMessage(ducrError, "Incorrect DUCR")
    }
  }
}
