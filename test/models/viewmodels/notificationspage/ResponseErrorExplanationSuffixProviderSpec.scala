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
import org.mockito.Mockito
import org.scalatestplus.mockito.MockitoSugar

class ResponseErrorExplanationSuffixProviderSpec extends BaseSpec with MockitoSugar {

  private trait Test {
    val appConfig = mock[AppConfig]
    val provider = new ResponseErrorExplanationSuffixProvider(appConfig)
  }

  "Response Error Explanation Switch" should {

    "return CDS when response-error-explanation-mode is set to CDS in config" in new Test {

      Mockito.when(appConfig.responseErrorExplanationMode).thenReturn("CDS")

      provider.suffix mustBe ".CDS"
    }

    "return Exports when response-error-explanation-mode is set to Exports in config" in new Test {

      Mockito.when(appConfig.responseErrorExplanationMode).thenReturn("Exports")

      provider.suffix mustBe ".Exports"
    }

    "return Exports for any other value" in new Test {

      Mockito.when(appConfig.responseErrorExplanationMode).thenReturn("OtherValue")

      provider.suffix mustBe ".Exports"
    }
  }

}
