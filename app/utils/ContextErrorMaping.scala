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

package utils

import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint

case class ContextErrorMapping[T](origin: Mapping[T]) extends Mapping[T] {
  override val key: String = origin.key
  override val mappings: Seq[Mapping[_]] = origin.mappings
  override val constraints: Seq[Constraint[T]] = origin.constraints

  private def fixErrors(errors: Seq[FormError]): Seq[FormError] =
    errors.map {
      case formError if formError.key.nonEmpty =>
        formError
      case formError =>
        origin.mappings.collectFirst {
          case mappingForTest if mappingForTest.key.nonEmpty && formError.message.startsWith(mappingForTest.key) =>
            formError.copy(mappingForTest.key)
        }.getOrElse(formError)
    }

  override def bind(data: Map[String, String]): Either[Seq[FormError], T] =
    origin.bind(data).left.map(fixErrors)

  override def unbind(value: T): Map[String, String] = origin.unbind(value)

  override def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) = {
    val (originMap, originErrors) = origin.unbindAndValidate(value)
    (originMap, fixErrors(originErrors))
  }

  override def withPrefix(prefix: String): Mapping[T] = copy(origin.withPrefix(prefix))

  override def verifying(constraints: Constraint[T]*): Mapping[T] = copy(origin.verifying(constraints: _*))
}
