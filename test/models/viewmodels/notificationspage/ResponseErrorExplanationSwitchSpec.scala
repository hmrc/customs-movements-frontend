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

package models.viewmodels.notificationspage

import base.BaseSpec
import config.AppConfig
import models.viewmodels.notificationspage.ResponseErrorExplanationMode._
import org.mockito.Mockito
import org.scalatestplus.mockito.MockitoSugar

class ResponseErrorExplanationSwitchSpec extends BaseSpec with MockitoSugar {

  private trait Test {
    val appConfig = mock[AppConfig]
    val switch = new ResponseErrorExplanationSwitch(appConfig)
  }

  "Response Error Explanation Switch" should {

    "return CDS when response-error-explanation-mode is set to CDS in config" in new Test {

      Mockito.when(appConfig.responseErrorExplanationMode).thenReturn("CDS")

      switch.getResponseErrorExplanationMode mustBe CDS
    }

    "return Exports when response-error-explanation-mode is set to Exports in config" in new Test {

      Mockito.when(appConfig.responseErrorExplanationMode).thenReturn("Exports")

      switch.getResponseErrorExplanationMode mustBe Exports
    }

    "return Exports for any other value" in new Test {

      Mockito.when(appConfig.responseErrorExplanationMode).thenReturn("OtherValue")

      switch.getResponseErrorExplanationMode mustBe Exports
    }
  }

}
