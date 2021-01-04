/*
 * Copyright 2021 HM Revenue & Customs
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

import com.codahale.metrics.{Counter, MetricRegistry, Timer}
import com.kenshoo.play.metrics.Metrics
import metrics.MovementsMetrics

trait MovementsMetricsStub {

  val registry = new MetricRegistry()

  private val metrics: Metrics = new Metrics {
    override val defaultRegistry: MetricRegistry = registry

    override def toJson: String = ???
  }

  val movementsMetricsStub = new MovementsMetrics(metrics)

  def timer(name: String): Timer = registry.getTimers.get(name)

  def counter(name: String): Counter = registry.getCounters.get(name)

}
