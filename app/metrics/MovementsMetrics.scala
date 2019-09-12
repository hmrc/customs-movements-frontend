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
import forms.Choice.AllowedChoiceValues._
import javax.inject.{Inject, Singleton}
import metrics.MetricIdentifiers._

@Singleton
class MovementsMetrics @Inject()(metrics: Metrics) {

  val timers = Map(
    Arrival -> metrics.defaultRegistry.timer(s"$arrivalMetric.timer"),
    Departure -> metrics.defaultRegistry.timer(s"$departureMetric.timer"),
    AssociateDUCR -> metrics.defaultRegistry.timer(s"$consolidationMetric.timer"),
    DisassociateDUCR -> metrics.defaultRegistry.timer(s"$disassociationMetric.timer"),
    ShutMucr -> metrics.defaultRegistry.timer(s"$shutMucr.timer")
  )

  val counters = Map(
    Arrival -> metrics.defaultRegistry.counter(s"$arrivalMetric.counter"),
    Departure -> metrics.defaultRegistry.counter(s"$departureMetric.counter"),
    AssociateDUCR -> metrics.defaultRegistry.counter(s"$consolidationMetric.counter"),
    DisassociateDUCR -> metrics.defaultRegistry.counter(s"$disassociationMetric.counter"),
    ShutMucr -> metrics.defaultRegistry.counter(s"$shutMucr.counter")
  )

  def startTimer(feature: String): Context = timers(feature).time()

  def incrementCounter(feature: String): Unit = counters(feature).inc()
}

object MetricIdentifiers {
  val arrivalMetric = "arrival"
  val departureMetric = "departure"
  val consolidationMetric = "consolidation"
  val disassociationMetric = "disassociation"
  val shutMucr = "shut"
}
