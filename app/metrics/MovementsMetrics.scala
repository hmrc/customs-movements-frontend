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

package metrics

import com.codahale.metrics.Timer.Context
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import forms.Choice
import forms.Choice._
import javax.inject.{Inject, Singleton}
import metrics.MetricIdentifiers._

@Singleton
class MovementsMetrics @Inject() (metrics: Metrics) {

  val timers = Map(
    Arrival.value -> metrics.defaultRegistry.timer(s"$arrivalMetric.timer"),
    Departure.value -> metrics.defaultRegistry.timer(s"$departureMetric.timer"),
    AssociateUCR.value -> metrics.defaultRegistry.timer(s"$associationMetric.timer"),
    DisassociateUCR.value -> metrics.defaultRegistry.timer(s"$disassociationMetric.timer"),
    ShutMUCR.value -> metrics.defaultRegistry.timer(s"$shutMucr.timer")
  )

  val counters = Map(
    Arrival.value -> metrics.defaultRegistry.counter(s"$arrivalMetric.counter"),
    Departure.value -> metrics.defaultRegistry.counter(s"$departureMetric.counter"),
    AssociateUCR.value -> metrics.defaultRegistry.counter(s"$associationMetric.counter"),
    DisassociateUCR.value -> metrics.defaultRegistry.counter(s"$disassociationMetric.counter"),
    ShutMUCR.value -> metrics.defaultRegistry.counter(s"$shutMucr.counter")
  )

  def startTimer(feature: String): Context = timers(feature).time()

  def startTimer(feature: Choice): Context = timers(feature.value).time()

  def incrementCounter(feature: String): Unit = counters(feature).inc()

  def incrementCounter(feature: Choice): Unit = counters(feature.value).inc()
}

object MetricIdentifiers {
  val arrivalMetric = "arrival"
  val departureMetric = "departure"
  val associationMetric = "association"
  val disassociationMetric = "disassociation"
  val shutMucr = "shut"
}
