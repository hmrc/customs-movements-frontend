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

package views.spec

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.MustMatchers
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}
import play.api.i18n.Messages
import play.api.mvc.Call

import scala.collection.JavaConverters._

//noinspection ScalaStyle
trait ViewMatchers { self: MustMatchers =>

  implicit private def elements2Scala(elements: Elements): Iterator[Element] = elements.iterator().asScala

  private def actualContentWas(node: Element): String =
    if (node == null) {
      "Element did not exist"
    } else {
      s"\nActual content was:\n${node.html}\n"
    }

  private def actualContentWas(node: Elements): String =
    if (node == null) {
      "Elements did not exist"
    } else {
      s"\nActual content was:\n${node.html}\n"
    }

  class ContainElementWithIDMatcher(id: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementById(id) != null,
        s"Document did not contain element with ID {$id}\n${actualContentWas(left)}",
        s"Document contained an element with ID {$id}"
      )
  }

  class ContainElementWithClassMatcher(name: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByClass(name).size() > 0,
        s"Document did not contain element with class {$name}\n${actualContentWas(left)}",
        s"Document contained an element with class {$name}"
      )
  }

  class ContainElementWithAttribute(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && !left.getElementsByAttributeValue(key, value).isEmpty,
        s"Document did not contain element with Attribute {$key=$value}\n${actualContentWas(left)}",
        s"Document contained an element with Attribute {$key=$value}"
      )
  }

  class ContainElementWithTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && !left.getElementsByTag(tag).isEmpty,
        s"Document did not contain element with Tag {$tag}\n${actualContentWas(left)}",
        s"Document contained an element with Tag {$tag}"
      )
  }

  class ElementHasClassMatcher(clazz: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.classNames().contains(clazz),
        s"Element did not have class {$clazz}\n${actualContentWas(left)}",
        s"Element had class {$clazz}"
      )
  }

  class ElementContainsTextMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.text().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(left)}",
        s"Element contained {$content}"
      )
  }

  class ElementContainsMessageMatcher(key: String, args: Seq[Any])(implicit messages: Messages) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val message = messages(key, args: _*)
      MatchResult(
        left != null && left.text().contains(message),
        s"Element did not contain message {$message}\n${actualContentWas(left)}",
        s"Element contained message {$message}"
      )
    }
  }

  class MessageIsDefinedAt(key: String)(implicit messages: Messages) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(messages.isDefinedAt(key), s"Message key {$key} is not in the messages file", s"Message is configured for key {$key}")
  }

  class ElementContainsHtmlMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.html().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(left)}",
        s"Element contained {$content}"
      )
  }

  class ElementContainsChildWithTextMatcher(tag: String, content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val elements = left.getElementsByTag(tag)
      MatchResult(
        left != null && elements.text().contains(content),
        s"Element did not contain text {$content}\n${actualContentWas(elements)}",
        s"Element contained text {$content}"
      )
    }
  }

  class ElementContainsChildWithAttributeMatcher(tag: String, key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.getElementsByTag(tag).attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
  }

  class ElementHasAttributeValueMatcher(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
  }

  class ElementHasAttributeMatcher(key: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(left != null && left.hasAttr(key), s"Element didnt have attribute {$key}", s"Element had attribute {$key}")
  }

  class ElementHasChildCountMatcher(count: Int) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(
        left != null && left.children().size() == count,
        s"Element had child count {${left.children().size()}}, expected {$count}",
        s"Element had child count {$count}"
      )
  }

  class ElementsHasSizeMatcher(size: Int) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult =
      MatchResult(left != null && left.size() == size, s"Elements had size {${left.size()}}, expected {$size}", s"Elements had size {$size}")
  }

  class ElementTagMatcher(tag: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(left != null && left.tagName() == tag, s"Elements had tag {${left.tagName()}}, expected {$tag}", s"Elements had tag {$tag}")
  }

  class ElementContainsFieldError(fieldName: String, content: String = "") extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val element = left.getElementById(s"error-message-$fieldName-input")
      val fieldErrorElement = if (element == null) left else element
      MatchResult(
        fieldErrorElement.text().contains(content),
        s"Element did not contain {$content}\n${actualContentWas(fieldErrorElement)}",
        s"Element contained {$content}"
      )
    }
  }

  class ElementContainsFieldErrorLink(fieldName: String, link: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      val element = left.getElementById(s"$fieldName-error")
      MatchResult(element != null && element.attr("href") == link, s"View not contains $fieldName with $link", s"View contains $fieldName with $link")
    }
  }

  class TranslationKeyMatcher(key: String) extends Matcher[Messages] {
    override def apply(left: Messages): MatchResult = MatchResult(
      matches = left.isDefinedAt(key),
      rawFailureMessage = s"$key is not defined in Messages",
      rawNegatedFailureMessage = s"$key is defined in Messages"
    )
  }

  class ChildMatcherBuilder(tag: String) {
    def containingText(text: String) = new ElementContainsChildWithTextMatcher(tag, text)
    def withAttribute(key: String, value: String) = new ElementContainsChildWithAttributeMatcher(tag, key, value)
  }

  def containElementWithID(id: String): Matcher[Element] = new ContainElementWithIDMatcher(id)
  def containElementWithClass(name: String): Matcher[Element] = new ContainElementWithClassMatcher(name)
  def containElementWithAttribute(key: String, value: String): Matcher[Element] =
    new ContainElementWithAttribute(key, value)
  def containElementWithTag(tag: String): Matcher[Element] = new ContainElementWithTagMatcher(tag)
  def containText(text: String): Matcher[Element] = new ElementContainsTextMatcher(text)
  def containMessage(key: String, args: Any*)(implicit messages: Messages): Matcher[Element] =
    new ElementContainsMessageMatcher(key, args)
  def haveClass(text: String): Matcher[Element] = new ElementHasClassMatcher(text)
  def containHtml(text: String): Matcher[Element] = new ElementContainsHtmlMatcher(text)
  def haveSize(size: Int): Matcher[Elements] = new ElementsHasSizeMatcher(size)
  def haveAttribute(key: String, value: String): Matcher[Element] = new ElementHasAttributeValueMatcher(key, value)
  def haveAttribute(key: String): Matcher[Element] = new ElementHasAttributeMatcher(key)
  def haveTag(tag: String): Matcher[Element] = new ElementTagMatcher(tag)
  def haveChildCount(count: Int): Matcher[Element] = new ElementHasChildCountMatcher(count)
  def haveChild(tag: String) = new ChildMatcherBuilder(tag)

  def haveFieldError(fieldName: String, content: String): Matcher[Element] =
    new ContainElementWithIDMatcher(s"error-message-$fieldName-input") and new ElementContainsFieldError(fieldName, content)
  def haveFieldErrorLink(fieldName: String, link: String): Matcher[Element] = new ElementContainsFieldErrorLink(fieldName, link)

  def haveGlobalErrorSummary: Matcher[Element] = new ContainElementWithIDMatcher("error-summary-heading")

  def haveHref(value: Call): Matcher[Element] = new ElementHasAttributeValueMatcher("href", value.url)
  def haveHref(url: String): Matcher[Element] = new ElementHasAttributeValueMatcher("href", url)

  def haveTranslationFor(key: String): Matcher[Messages] = new TranslationKeyMatcher(key)

  val checked: BeMatcher[Element] = new BeMatcher[Element] {
    override def apply(left: Element): MatchResult =
      MatchResult(left.attr("checked") == "checked", "Element is not checked", "Element is checked")
  }

  val unchecked: BeMatcher[Element] = not(checked)
}
