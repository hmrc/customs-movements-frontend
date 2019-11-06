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

package unit.controllers.consolidations

import controllers.consolidations.AssociateUcrConfirmationController
import forms.Choice
import forms.Choice.AssociateDUCR
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.associate_ucr_confirmation

class AssociateUcrConfirmationControllerSpec extends ControllerSpec {

  private val mockAssociateDucrConfirmPage = mock[associate_ucr_confirmation]

  private val controller =
    new AssociateUcrConfirmationController(mockAuthAction, mockJourneyAction, stubMessagesControllerComponents(), mockAssociateDucrConfirmPage)

  override def beforeEach() {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(AssociateDUCR))
    when(mockAssociateDucrConfirmPage.apply()(any(), any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(mockAssociateDucrConfirmPage)

    super.afterEach()
  }

  "Associate DUCR Confirmation controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
        verify(mockAssociateDucrConfirmPage).apply()(any(), any(), any())
      }
    }
  }
}
