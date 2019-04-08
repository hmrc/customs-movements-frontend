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

package metrics

import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import metrics.MetricIdentifiers._

@Singleton
class MovementsMetrics @Inject()(metrics: Metrics) {

  private val timers = Map(
    submissionMetric -> metrics.defaultRegistry.timer(
      s"$submissionMetric.timer"),
    arrivalMetric -> metrics.defaultRegistry.timer(s"$arrivalMetric.timer"),
    departureMetric -> metrics.defaultRegistry.timer(s"$departureMetric.timer"),
    consolidationMetric -> metrics.defaultRegistry.timer(
      s"$consolidationMetric.timer"),
    cancelMetric -> metrics.defaultRegistry.timer(s"$cancelMetric.timer")
  )

  private val counters = Map(
    submissionMetric -> metrics.defaultRegistry.counter(
      s"$submissionMetric.counter"),
    arrivalMetric -> metrics.defaultRegistry.counter(s"$arrivalMetric.counter"),
    departureMetric -> metrics.defaultRegistry.counter(
      s"$departureMetric.counter"),
    consolidationMetric -> metrics.defaultRegistry.counter(
      s"$consolidationMetric.counter"),
    cancelMetric -> metrics.defaultRegistry.counter(s"$cancelMetric.counter")
  )

  def startTimer(feature: String): Context = timers(feature).time()

  def incrementCounter(feature: String): Unit = counters(feature).inc()
}

object MetricIdentifiers {
  val submissionMetric = "submission"
  val arrivalMetric = "movement.arrival"
  val departureMetric = "movement.departure"
  val consolidationMetric = "consolidation"
  val cancelMetric = "cancellation"

}
