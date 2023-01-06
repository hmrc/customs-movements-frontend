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

package views

import base.Injector
import controllers.routes.ChoiceController
import play.api.test.FakeRequest
import views.html.components.gds.siteHeader

class SiteHeaderViewSpec extends ViewSpec with Injector {

  "SiteHeader component" should {
    "render service name with link to 'Choice' page" in {
      implicit val request = FakeRequest().withCSRFToken
      val headerComponent = instanceOf[siteHeader]
      val serviceNameLink = headerComponent()
        .getElementsByClass("hmrc-header__service-name--linked")
        .first()
      serviceNameLink must haveHref(ChoiceController.displayChoices)
      serviceNameLink must containMessage("service.name")
    }
  }
}
