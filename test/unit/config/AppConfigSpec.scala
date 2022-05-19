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

package config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val environment = Environment.simple()
  private val validAppConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
        |urls.customsDeclarationsGoodsTakenOutOfEu="https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
        |urls.serviceAvailability="https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
        |urls.exitSurveyUrl="http://localhost:9514/feedback/customs-movements-frontend"
        |urls.eoriService = "https://www.gov.uk/eori"
        |urls.cdsRegister = "https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service"
        |urls.cdsCheckStatus = "https://www.tax.service.gov.uk/customs/register-for-cds/are-you-based-in-uk"
        |urls.previousProcedureCodes = "http://previousProcedureCodes"
        |urls.customsDecCompletionRequirements="http://customsDecCompletionRequirements"
        |urls.locationCodeForAirports="http://locationCodeForAirports"
        |urls.certificateOfAgreementAirports="http://certificateOfAgreementAirports"
        |urls.locationCodeForMaritimePorts="http://locationCodeForMaritimePorts"
        |urls.locationCodeForTempStorage="http://locationCodeForTempStorage"
        |urls.designatedExportPlaceCodes="http://designatedExportPlaceCodes"
        |urls.locationCodesForCsePremises="http://locationCodesForCsePremises"
        |urls.goodsLocationCodesForDataElement="http://goodsLocationCodesForDataElement"
        |urls.tariffCdsChiefSupplement="http://tariffCdsChiefSupplement"
        |
        |mongodb.uri="mongodb://localhost:27017/customs-movements-frontend"
        |
        |microservice.services.auth.host=localhostauth
        |google-analytics.token=N/A
        |google-analytics.host=localhostGoogle
        |tracking-consent-frontend.gtm.container=a
        |
        |countryCodesCsvFilename=code_lists/mdg-country-codes.csv
        |countryCodesJsonFilename=location-autocomplete-canonical-list.json
        |euCountryCodesCsvFilename=code_lists/mdg-country-codes-eu.csv
        |
        |microservice.services.nrs.host=localhostnrs
        |microservice.services.nrs.port=7654
        |microservice.services.nrs.apikey=cds-exports
        |microservice.services.features.default=disabled
        |microservice.services.features.response-error-explanation-mode=Exports
        |microservice.services.auth.port=9988
        |
        |microservice.services.customs-declare-exports-movements.host=localhost
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.fetch-notifications=/notifications
        |microservice.services.customs-declare-exports-movements.fetch-all-submissions=/movements
        |microservice.services.customs-declare-exports-movements.fetch-single-submission=/movements
        |
        |microservice.services.customs-declare-exports-movements.submit-movements=/movements
        |microservice.services.customs-declare-exports-movements.submit-consolidation=/consolidation
        |
        |microservice.services.contact-frontend.url=/contact-frontend-url
        |microservice.services.contact-frontend.serviceId=Movements-Service-ID
        |play.frontend.host="self/base-url"
      """.stripMargin
    )

  private val missingValuesAppConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
        |urls.customsDeclarationsGoodsTakenOutOfEu="https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
        |urls.serviceAvailability="https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
        |urls.exitSurveyUrl="http://localhost:9514/feedback/customs-movements-frontend"
        |urls.eoriService = "https://www.gov.uk/eori"
        |urls.cdsRegister = "https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service"
        |urls.cdsCheckStatus = "https://www.tax.service.gov.uk/customs/register-for-cds/are-you-based-in-uk"
        |urls.previousProcedureCodes = "http://previousProcedureCodes"
        |urls.customsDecCompletionRequirements="http://customsDecCompletionRequirements"
        |urls.locationCodeForAirports="http://locationCodeForAirports"
        |urls.certificateOfAgreementAirports="http://certificateOfAgreementAirports"
        |urls.locationCodeForMaritimePorts="http://locationCodeForMaritimePorts"
        |urls.locationCodeForTempStorage="http://locationCodeForTempStorage"
        |urls.designatedExportPlaceCodes="http://designatedExportPlaceCodes"
        |urls.locationCodesForCsePremises="http://locationCodesForCsePremises"
        |urls.goodsLocationCodesForDataElement="http://goodsLocationCodesForDataElement"
        |urls.tariffCdsChiefSupplement="http://tariffCdsChiefSupplement"
        |
        |mongodb.uri="mongodb://localhost:27017/customs-movements-frontend"
        |
        |google-analytics.token=N/A
        |google-analytics.host=localhostGoogle
        |
        |countryCodesCsvFilename=code_lists/mdg-country-codes.csv
        |countryCodesJsonFilename=location-autocomplete-canonical-list.json
        |euCountryCodesCsvFilename=code_lists/mdg-country-codes-eu.csv
        |
        |microservice.services.nrs.host=localhostnrs
        |microservice.services.nrs.port=7654
        |microservice.services.nrs.apikey=cds-exports
        |microservice.services.features.default=disabled
        |microservice.services.features.response-error-explanation-mode=Exports
        |microservice.services.auth.port=9988
        |
        |microservice.services.contact-frontend.url=/contact-frontend-url
        |microservice.services.contact-frontend.serviceId=Movements-Service-ID
      """.stripMargin
    )

  private val validServicesConfiguration = Configuration(validAppConfig)
  private val missingValuesServicesConfiguration = Configuration(missingValuesAppConfig)

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")

  val validConfigService: AppConfig = appConfig(validServicesConfiguration)
  val emptyConfigService: AppConfig = appConfig(missingValuesServicesConfiguration)

  "The config" should {

    "have analytics token" in {
      validConfigService.analyticsToken must be("N/A")
    }

    "have analytics host" in {
      validConfigService.analyticsHost must be("localhostGoogle")
    }

    "have gtm container" in {
      validConfigService.gtmContainer must be("a")
    }

    "have auth URL" in {
      validConfigService.authUrl must be("http://localhostauth:9988")
    }

    "have login URL" in {
      validConfigService.loginUrl must be("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    // what is continue URL - redirect ?
    "have login continue URL" in {
      validConfigService.loginContinueUrl must be("http://localhost:9000/customs-declare-exports-frontend")
    }

    "have previousProcedureCodesUrl URL" in {
      validConfigService.previousProcedureCodesUrl must be("http://previousProcedureCodes")
    }

    "have customsDecCompletionRequirements URL" in {
      validConfigService.customsDecCompletionRequirements must be("http://customsDecCompletionRequirements")
    }

    "have locationCodeForAirports URL" in {
      validConfigService.locationCodeForAirports must be("http://locationCodeForAirports")
    }

    "have certificateOfAgreementAirports URL" in {
      validConfigService.certificateOfAgreementAirports must be("http://certificateOfAgreementAirports")
    }

    "have locationCodeForMaritimePorts URL" in {
      validConfigService.locationCodeForMaritimePorts must be("http://locationCodeForMaritimePorts")
    }

    "have locationCodeForTempStorage URL" in {
      validConfigService.locationCodeForTempStorage must be("http://locationCodeForTempStorage")
    }

    "have designatedExportPlaceCodes URL" in {
      validConfigService.designatedExportPlaceCodes must be("http://designatedExportPlaceCodes")
    }

    "have locationCodesForCsePremises URL" in {
      validConfigService.locationCodesForCsePremises must be("http://locationCodesForCsePremises")
    }

    "have goodsLocationCodesForDataElement URL" in {
      validConfigService.goodsLocationCodesForDataElement must be("http://goodsLocationCodesForDataElement")
    }

    "have tariffCdsChiefSupplement URL" in {
      validConfigService.tariffCdsChiefSupplement must be("http://tariffCdsChiefSupplement")
    }

    "have language map with English" in {
      validConfigService.languageMap.get("english").isDefined must be(true)
    }

    "have language map with Cymraeg" in {
      validConfigService.languageMap.get("cymraeg").isDefined must be(true)
    }

    "have movements backend hostname " in {
      validConfigService.customsDeclareExportsMovements must be("http://localhost:9876")
    }

    "have movements submission URL" in {
      validConfigService.movementsSubmissionUri must be("/movements")
    }

    "have movement consolidation submission URL" in {
      validConfigService.movementConsolidationUri must be("/consolidation")
    }

    "have fetch all submissions URL" in {
      validConfigService.fetchAllSubmissions must be("/movements")
    }

    "have fetch single submission" in {
      validConfigService.fetchSingleSubmission must be("/movements")
    }

    "have fetch notification URL" in {
      validConfigService.fetchNotifications must be("/notifications")
    }

    "have selfBaseUrl" in {
      validConfigService.selfBaseUrl must be(defined)
      validConfigService.selfBaseUrl.get must be("self/base-url")
    }

    "have feedback link" in {
      validConfigService.giveFeedbackLink must be("/contact-frontend-url?service=Movements-Service-ID")
    }

    "empty selfBaseUrl when the key is missing" in {
      emptyConfigService.selfBaseUrl must be(None)
    }

    "throw an exception" when {

      "gtm.container is missing" in {
        intercept[Exception](emptyConfigService.gtmContainer).getMessage must be(
          "Could not find config key 'tracking-consent-frontend.gtm.container'"
        )
      }

      "auth.host is missing" in {
        intercept[Exception](emptyConfigService.authUrl).getMessage must be("Could not find config key 'auth.host'")
      }

      "customs-declare-exports-movements.host is missing" in {
        intercept[Exception](emptyConfigService.customsDeclareExportsMovements).getMessage must be(
          "Could not find config key 'customs-declare-exports-movements.host'"
        )
      }

      "movement Arrival submission uri is missing" in {
        intercept[Exception](emptyConfigService.movementsSubmissionUri).getMessage must be(
          "Missing configuration for Customs Declarations Exports Movements submission URI"
        )
      }

      "consolidation submission uri is missing" in {
        intercept[Exception](emptyConfigService.movementConsolidationUri).getMessage must be(
          "Missing configuration for Customs Declarations Exports Movements Consolidation"
        )
      }

      "fetch all submissions uri is missing" in {
        intercept[Exception](emptyConfigService.fetchAllSubmissions).getMessage must be(
          "Missing configuration for Customs Declaration Exports fetch all submission URI"
        )
      }

      "fetch single submission uri is missing" in {
        intercept[Exception](emptyConfigService.fetchSingleSubmission).getMessage must be(
          "Missing configuration for Customs Declaration Exports fetch single submission URI"
        )
      }

      "fetch notifications uri is missing" in {
        intercept[Exception](emptyConfigService.fetchNotifications).getMessage must be(
          "Missing configuration for Customs Declarations Exports fetch notifications URI"
        )
      }
    }
  }

}
