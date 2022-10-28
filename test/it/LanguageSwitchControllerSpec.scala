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

import org.scalatest.OptionValues
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LanguageSwitchControllerSpec extends IntegrationSpec with OptionValues {

  private val english = "english"
  private val welsh = "cymraeg"
  private val fakeUrl: String = "fakeUrl"

  val requestHeaders: Headers = new Headers(Seq(("Referer", fakeUrl)))

  private def switchLanguageRoute(lang: String): String = controllers.routes.LanguageSwitchController.switchToLanguage(lang).url

  "LanguageSwitch Controller" when {

      "English selected" must {
        "switch to English" in {

          val application = new GuiceApplicationBuilder()
            .build()

          val request = FakeRequest(GET, switchLanguageRoute(english)).withHeaders(requestHeaders)

          val result = route(fakeApplication(), request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual fakeUrl

          cookies(result).find(_.name == "PLAY_LANG").get.value mustEqual "en"

          application.stop()
        }
      }

      "Welsh selected" must {
        "switch to Welsh" in {

          val application = new GuiceApplicationBuilder()
            .build()

          val request = FakeRequest(GET, switchLanguageRoute(welsh)).withHeaders(requestHeaders)

          val result = route(fakeApplication(), request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual fakeUrl

          cookies(result).find(_.name == "PLAY_LANG").get.value mustEqual "cy"

          application.stop()
        }
      }

  }
}
