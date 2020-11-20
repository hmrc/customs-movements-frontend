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

import com.google.inject.{Inject, Singleton}
import javax.inject.Named
import mongock.MongockConfig
import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig,
  @Named("appName") appName: String
) {

  private def loadConfig(key: String): String =
    runModeConfiguration
      .getOptional[String](key)
      .getOrElse(throw new Exception(s"Missing configuration key: $key"))

  runModeConfiguration
    .getOptional[String]("mongodb.uri")
    .map(uri => MongockConfig(uri))

  private lazy val contactFrontendBaseUrl = servicesConfig.baseUrl("contact-frontend")
  lazy val contactFrontendServiceIdentifier = loadConfig("microservice.services.contact-frontend.serviceId")

  lazy val keyStoreSource: String = appName
  lazy val keyStoreUrl: String = servicesConfig.baseUrl("keystore")
  lazy val sessionCacheDomain: String =
    servicesConfig.getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = servicesConfig.baseUrl("auth")
  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")
  lazy val customsDeclarationsGoodsTakenOutOfEuUrl = loadConfig("urls.customsDeclarationsGoodsTakenOutOfEu")
  lazy val tradeTariffUrl = loadConfig("urls.tradeTariff")
  lazy val serviceAvailabilityUrl = loadConfig("urls.serviceAvailability")

  lazy val customsDeclareExportsMovements = servicesConfig.baseUrl("customs-declare-exports-movements")

  lazy val movementsSubmissionUri = servicesConfig.getConfString(
    "customs-declare-exports-movements.submit-movements",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports Movements submission URI")
  )

  lazy val movementConsolidationUri = servicesConfig.getConfString(
    "customs-declare-exports-movements.submit-consolidation",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports Movements Consolidation")
  )

  lazy val fetchAllSubmissions = servicesConfig.getConfString(
    "customs-declare-exports-movements.fetch-all-submissions",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports fetch all submission URI")
  )

  lazy val fetchSingleSubmission = servicesConfig.getConfString(
    "customs-declare-exports-movements.fetch-single-submission",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports fetch single submission URI")
  )

  lazy val fetchNotifications = servicesConfig.getConfString(
    "customs-declare-exports-movements.fetch-notifications",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports fetch notifications URI")
  )

  lazy val ileQueryUri = servicesConfig.getConfString(
    "customs-declare-exports-movements.ile-query",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports ile query URI")
  )

  lazy val responseErrorExplanationMode = loadConfig("microservice.services.features.response-error-explanation-mode")

  lazy val languageTranslationEnabled =
    runModeConfiguration
      .getOptional[Boolean]("microservice.services.features.welsh-translation")
      .getOrElse(true)

  lazy val gtmContainer: String = servicesConfig.getString("tracking-consent-frontend.gtm.container")

  lazy val giveFeedbackLink: String =
    s"$contactFrontendBaseUrl/contact/beta-feedback-unauthenticated?service=$contactFrontendServiceIdentifier"

  def languageMap: Map[String, Lang] =
    Map("english" -> Lang("en"), "cymraeg" -> Lang("cy"))
}
