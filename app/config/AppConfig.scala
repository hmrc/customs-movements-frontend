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

package config

import com.google.inject.{Inject, Singleton}
import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Named

@Singleton
class AppConfig @Inject() (
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig,
  @Named("appName") namedAppName: String
) {

  private def loadConfig(key: String): String =
    runModeConfiguration
      .getOptional[String](key)
      .getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private def loadOptionalConfig(key: String): Option[String] =
    runModeConfiguration.getOptional[String](key)

  lazy val appName: String = namedAppName
  lazy val sessionCacheDomain: String =
    servicesConfig.getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))

  val maybeTdrHashSalt = loadOptionalConfig("secret.tdrHashSalt")

  val analyticsToken = loadConfig(s"google-analytics.token")
  val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = servicesConfig.baseUrl("auth")
  val loginUrl = loadConfig("urls.login")
  val loginContinueUrl = loadConfig("urls.loginContinue")

  val eoriService: String = loadConfig("urls.eoriService")
  val cdsRegister: String = loadConfig("urls.cdsRegister")
  val cdsCheckStatus: String = loadConfig("urls.cdsCheckStatus")

  val customsDecCompletionRequirements = loadConfig("urls.customsDecCompletionRequirements")
  val locationCodeForAirports = loadConfig("urls.locationCodeForAirports")
  val certificateOfAgreementAirports = loadConfig("urls.certificateOfAgreementAirports")
  val locationCodeForMaritimePorts = loadConfig("urls.locationCodeForMaritimePorts")
  val locationCodeForTempStorage = loadConfig("urls.locationCodeForTempStorage")
  val designatedExportPlaceCodes = loadConfig("urls.designatedExportPlaceCodes")
  val locationCodesForCsePremises = loadConfig("urls.locationCodesForCsePremises")
  val previousProcedureCodesUrl = loadConfig("urls.previousProcedureCodes")
  val goodsLocationCodesForDataElement = loadConfig("urls.goodsLocationCodesForDataElement")
  val tariffCdsChiefSupplement = loadConfig("urls.tariffCdsChiefSupplement")
  val guidanceOnDucrAndMucr = loadConfig("urls.guidanceOnDucrAndMucr")

  lazy val customsDeclareExportsMovements = servicesConfig.baseUrl("customs-declare-exports-movements")

  lazy val selfBaseUrl: Option[String] = loadOptionalConfig("play.frontend.host")
  val giveFeedbackLink: String =
    loadConfig("urls.exportsFeedbackForm")

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

  lazy val gtmContainer: String = servicesConfig.getString("tracking-consent-frontend.gtm.container")

  lazy val languages: Seq[String] = runModeConfiguration.get[Seq[String]]("play.i18n.langs")

  def languageMap: Map[String, Lang] =
    Map("english" -> Lang("en"), "cymraeg" -> Lang("cy"))
}
