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

package models.viewmodels.notificationspage

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger

@Singleton
class ResponseErrorExplanationSuffixProvider @Inject()(appConfig: AppConfig) {

  private val logger = Logger(this.getClass)

  private val AllowedConfigValues = Set("CDS", "Exports")
  private val DefaultSuffix = "Exports"

  lazy val suffix: String = {
    val mode = appConfig.responseErrorExplanationMode

    AllowedConfigValues.find(_ == mode) match {
      case Some(configValue) => s".$configValue"
      case None =>
        logger.info(
          s"Unknown value for configuration key 'microservice.services.features.response-error-explanation-mode': $mode"
        )
        s".$DefaultSuffix"
    }
  }

}
