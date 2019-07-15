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

package base
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.{Assertion, MustMatchers}
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

trait ViewValidator extends MustMatchers with ViewMatchers {

  private def asDocument(html: Html): Document = Jsoup.parse(html.toString())
  private def asDocument(page: String): Document = Jsoup.parse(page)
  protected def htmlBodyOf(result: Future[Result]): Document = asDocument(contentAsString(result))

  def getElementByCss(html: Html, selector: String): Element = {

    val elements = asDocument(html).select(selector)

    if (elements.isEmpty)
      throw new Exception(s"Can't find element $selector on page using CSS")

    elements.first()
  }

  def getElementByCss(page: String, selector: String): Element = {

    val elements = asDocument(page).select(selector)

    if (elements.isEmpty)
      throw new Exception(s"Can't find element $selector on page using CSS")

    elements.first()
  }

  def getElementsByCss(html: Html, selector: String): Elements =
    asDocument(html).select(selector)

  def getElementsByCss(page: String, selector: String): Elements =
    asDocument(page).select(selector)

  def getElementById(html: Html, id: String): Element = {

    val element = asDocument(html).getElementById(id)

    if (element == null)
      throw new Exception(s"Can't find element $id on page by id")

    element
  }

  def getElementById(page: String, id: String): Element = {

    val element = asDocument(page).getElementById(id)

    if (element == null)
      throw new Exception(s"Can't find element $id on page by id")

    element
  }

  def checkErrorsSummary(html: Html)(implicit messages: Messages): Unit = {
    getElementByCss(html, "#error-summary-heading").text() must be(messages("error.summary.title"))
    getElementByCss(html, "div.error-summary.error-summary--show>p")
      .text() must be(messages("error.summary.text"))
  }

  def checkErrorsSummary(page: String)(implicit messages: Messages): Unit = {

    getElementByCss(page, "#error-summary-heading").text() must be(messages("error.summary.title"))
    getElementByCss(page, "div.error-summary.error-summary--show>p")
      .text() must be(messages("error.summary.text"))
  }

  def checkErrorLink(page: String, child: Int, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = getElementByCss(page, "div.error-summary.error-summary--show>ul>li:nth-child(" + child + ")>a")

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }

  def checkErrorLink(html: Html, child: Int, error: String, href: String)(implicit messages: Messages): Unit = {

    val errorLink = getElementByCss(html, "div.error-summary.error-summary--show>ul>li:nth-child(" + child + ")>a")

    errorLink.text() must be(messages(error))
    errorLink.attr("href") must be(href)
  }

  def verifyUnchecked(view: Html, id: String): Assertion = getElementById(view, id).attr("checked") must be("")
  def verifyUnchecked(page: String, id: String): Assertion = getElementById(page, id).attr("checked") must be("")

  def verifyChecked(view: Html, id: String): Assertion = getElementById(view, id).attr("checked") must be("checked")
  def verifyChecked(page: String, id: String): Assertion = getElementById(page, id).attr("checked") must be("checked")
}
