/*
 * Copyright 2024 HM Revenue & Customs
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

package views.helpers

import base.{Injector, UnitSpec}
import forms.Choice.FindConsignment
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi
import play.twirl.api.Html

import java.util.Locale

class ChoicePageLinkHelperSpec extends UnitSpec with Injector {

  val instance = instanceOf[ChoicePageLinkHelper]
  implicit val messages: Messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())

  "ChoicePageLinkHelper" when {

    "Generating choices" in {
      val document: Document = instance.generateChoiceOption(FindConsignment)

      val div = document.getElementById("findConsignment")
      div.children().get(0).tagName() mustBe "h2"
      div.children().get(1).tagName() mustBe "p"
      div.children().get(2).tagName() mustBe "a"
    }
  }
}
