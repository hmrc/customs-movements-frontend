/*
 * Copyright 2020 HM Revenue & Customs
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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class ExternalServicesConfig @Inject()(val configuration: Configuration) {

  private def loadUrl(key: String): String =
    configuration.getOptional[String](s"urls.$key").getOrElse(throw new Exception(s"Missing configuration key: urls.$key"))

  val customsDeclarationsGoodsTakenOutOfEuUrl: String = loadUrl("customsDeclarationsGoodsTakenOutOfEu")
  val tradeTariffUrl: String = loadUrl("tradeTariff")
  val serviceAvailabilityUrl: String = loadUrl("serviceAvailability")
  val exitSurveyUrl: String = loadUrl("exitSurveyUrl")

  val eoriService: String = loadUrl("eoriService")
  val cdsRegister: String = loadUrl("cdsRegister")
  val cdsCheckStatus: String = loadUrl("cdsCheckStatus")
}
