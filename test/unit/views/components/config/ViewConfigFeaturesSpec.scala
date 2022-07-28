/*
 * Copyright 2022 HM Revenue & Customs
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

package views.components.config

import base.UnitSpec
import config.IleQueryConfig
import controllers.actions.ArriveDepartAllowList
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

trait ViewConfigFeaturesSpec extends BeforeAndAfterEach { self: UnitSpec =>

  val ileQueryEnabled = mock[IleQueryConfig]
  val ileQueryDisabled = mock[IleQueryConfig]

  val eoriInArriveDepartAllowList = mock[ArriveDepartAllowList]
  val eoriNotInArriveDepartAllowList = mock[ArriveDepartAllowList]

  override def beforeEach() {
    super.beforeEach()

    when(ileQueryEnabled.isIleQueryEnabled).thenReturn(true)
    when(ileQueryDisabled.isIleQueryEnabled).thenReturn(false)
    when(eoriInArriveDepartAllowList.contains(any())).thenReturn(true)
    when(eoriNotInArriveDepartAllowList.contains(any())).thenReturn(false)
  }

  override def afterEach(): Unit = {
    reset(ileQueryEnabled, ileQueryDisabled, eoriInArriveDepartAllowList, eoriNotInArriveDepartAllowList)
    super.afterEach()
  }
}
