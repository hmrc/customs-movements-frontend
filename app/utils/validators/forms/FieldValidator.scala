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

package utils.validators.forms

import java.util.regex.Pattern
import services.Countries.allCountries

import scala.util.{Success, Try}

object FieldValidator {

  implicit class PredicateOpsForFunctions[A](first: A => Boolean) {
    def and(second: A => Boolean): A => Boolean =
      (arg: A) => first(arg) && second(arg)

    def and(second: Boolean): A => Boolean = (arg: A) => first(arg) && second

    def or(second: A => Boolean): A => Boolean =
      (arg: A) => first(arg) || second(arg)

    def or(second: Boolean): A => Boolean = (arg: A) => first(arg) || second
  }

  implicit class PredicateOpsForBooleans[A](first: Boolean) {
    def and(second: A => Boolean): A => Boolean =
      (arg: A) => first && second(arg)

    def and(second: Boolean): Boolean = first && second

    def or(second: A => Boolean): A => Boolean =
      (arg: A) => first || second(arg)

    def or(second: Boolean): Boolean = first || second
  }

  private val zerosOnlyRegexValue = "[0]+"
  private val noMoreDecimalPlacesThanRegexValue: Int => String =
    (decimalPlaces: Int) => s"^([0-9]*)([\\.]{0,1}[0-9]{0,$decimalPlaces})$$"
  private val allowedSpecialChars = Set(',', '.', '-', '\'', '/', ' ')

  val isEmpty: String => Boolean = (input: String) => input.isEmpty

  val isEmptyOr: (String => Boolean) => String => Boolean = (f: String => Boolean) => (input: String) => isEmpty(input) || f(input)

  val nonEmpty: String => Boolean = (input: String) => input.nonEmpty

  val noLongerThan: Int => String => Boolean = (length: Int) => (input: String) => input.length <= length

  val noShorterThan: Int => String => Boolean = (length: Int) => (input: String) => input.length >= length

  val hasSpecificLength: Int => String => Boolean = (length: Int) => (input: String) => input.length == length

  val lengthInRange: Int => Int => String => Boolean = (min: Int) => (max: Int) => (input: String) => input.length >= min && input.length <= max

  val isNumeric: String => Boolean = (input: String) => input.forall(_.isDigit)

  val isAllCapitalLetter: String => Boolean = (input: String) => input.forall(_.isUpper)

  val isAlphabetic: String => Boolean = (input: String) => input.forall(_.isLetter)

  val isAlphanumeric: String => Boolean = (input: String) => input.forall(_.isLetterOrDigit)

  val isAlphanumericWithSpecialCharacters: Set[Char] => String => Boolean =
    (allowedChars: Set[Char]) => (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedChars)

  val isAlphanumericWithAllowedSpecialCharacters: String => Boolean =
    (input: String) => input.filter(!_.isLetterOrDigit).forall(allowedSpecialChars)

  val startsWithCapitalLetter: String => Boolean = (input: String) => input.headOption.exists(_.isUpper)

  val isContainedIn: Iterable[String] => String => Boolean =
    (iterable: Iterable[String]) => (input: String) => iterable.exists(_ == input)

  val isContainedInIgnoreCase: Iterable[String] => String => Boolean =
    (iterable: Iterable[String]) => (input: String) => iterable.exists(_.toUpperCase == input.toUpperCase)

  val containsNotOnlyZeros: String => Boolean = (input: String) => !input.matches(zerosOnlyRegexValue)

  val isTailNumeric: String => Boolean = (input: String) =>
    Try(input.tail) match {
      case Success(value) if value.nonEmpty => isNumeric(value)
      case _                                => false
    }

  val isInRange: (Int, Int) => String => Boolean = (min: Int, max: Int) =>
    (input: String) =>
      Try(input.toInt) match {
        case Success(value) => value >= min && value <= max
        case _              => false
      }

  val isValidCountryCode: String => Boolean = (input: String) => allCountries.exists(_.countryCode == input)

  val isDecimalWithNoMoreDecimalPlacesThan: Int => String => Boolean =
    (decimalPlaces: Int) => (input: String) => input.matches(noMoreDecimalPlacesThanRegexValue(decimalPlaces))

  val validateDecimal: Int => Int => String => Boolean = (totalLength: Int) =>
    (decimalPlaces: Int) =>
      (input: String) =>
        input.split('.') match {
          case Array(a, b) if isNumeric(a) && isNumeric(b) =>
            b.length <= decimalPlaces && (a + b).length <= totalLength
          case Array(a) if isNumeric(a) => a.length <= totalLength
          case _                        => false
        }

  val containsDuplicates: Iterable[_] => Boolean = (input: Iterable[_]) => input.toSet.size != input.size

  val areAllElementsUnique: Iterable[_] => Boolean = (input: Iterable[_]) => input.toSet.size == input.size

  val ofPattern: String => String => Boolean = (pattern: String) => (input: String) => input.matches(pattern)

  private val ducrPattern = Pattern.compile("[0-9]{1}[A-Z]{2}[0-9]{12}[-]{1}[-/()A-Z0-9]{1,19}")

  val validDucr: String => Boolean = ducrPattern.matcher(_).matches()

  val validDucrIgnoreCase: String => Boolean = (input: String) => validDucr(input.toUpperCase)

  val validMucr: String => Boolean = (input: String) =>
    input.matches("""GB/[0-9A-Z]{3,4}-[0-9A-Z]{5,28}|GB/[0-9A-Z]{9,12}-[0-9A-Z]{1,23}|A:[0-9A-Z]{3}[0-9]{8}|C:[A-Z]{3}[0-9A-Z]{3,30}""")

  val validMucrIgnoreCase: String => Boolean = (input: String) => validMucr(input.toUpperCase) && noLongerThan(35)(input)

  def validRegex: String = "^[0-9]{0,3}[A-Z]?$"

  val isValidDucrPartId: String => Boolean = (input: String) => lengthInRange(1)(4)(input) && input.matches(validRegex)
}
