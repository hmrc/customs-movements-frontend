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

import com.codahale.metrics.Counting
import org.scalatest.matchers.{MatchResult, Matcher}

trait MetricsMatchers {
  def changeOn(block: => Any): Matcher[Counting] = new MetricsMatchers.ChangeOnMatcher(() => block)
}

object MetricsMatchers {
  class ChangeOnMatcher(block: () => Any) extends Matcher[Counting] {
    override def apply(left: Counting): MatchResult = {
      val before = left.getCount
      block()
      val after = left.getCount
      MatchResult(after > before, "Metric does not change in block", "Metric changed in block")
    }
  }
}
