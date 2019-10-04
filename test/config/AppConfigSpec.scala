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

package config

import base.MovementBaseSpec
import com.typesafe.config.{Config, ConfigFactory}
import features.{Feature, FeatureStatus}
import play.api.Mode.Test
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

class AppConfigSpec extends MovementBaseSpec {

  val config = app.injector.instanceOf[AppConfig]
  val environment = Environment.simple()

  private val validAppConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
        |urls.customsDeclarationsGoodsTakenOutOfEu="https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
        |urls.serviceAvailability="https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
        |
        |microservice.services.auth.host=localhostauth
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
        |microservice.services.features.welsh-translation=false
        |microservice.services.auth.port=9988
        |
        |microservice.services.customs-declare-exports-movements.host=localhost
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.fetch-notifications=/notifications
        |microservice.services.customs-declare-exports-movements.fetch-all-submissions=/movements
        |microservice.services.customs-declare-exports-movements.fetch-single-submission=/movements
        |
        |microservice.services.customs-declare-exports-movements.submit-movement-arrival=/movements/arrival
        |microservice.services.customs-declare-exports-movements.submit-movement-departure=/movements/departure
        |microservice.services.customs-declare-exports-movements.submit-consolidation=/consolidation
      """.stripMargin
    )
  private val emptyAppConfig: Config = ConfigFactory.parseString("")
  val validServicesConfiguration = Configuration(validAppConfig)
  private val emptyServicesConfiguration = Configuration(emptyAppConfig)

  private def runMode(conf: Configuration): RunMode = new RunMode(conf, Test)
  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf, runMode(conf))
  private def appConfig(conf: Configuration) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")

  val validConfigService: AppConfig = appConfig(validServicesConfiguration)
  val emptyConfigService: AppConfig = appConfig(emptyServicesConfiguration)

  "The config" should {

    "have analytics token" in {
      validConfigService.analyticsToken must be("N/A")
    }

    "have analytics host" in {
      validConfigService.analyticsHost must be("localhostGoogle")
    }

    "have auth URL" in {
      validConfigService.authUrl must be("http://localhostauth:9988")
    }

    "have login URL" in {
      validConfigService.loginUrl must be("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    "have customsDeclarationsGoodsTakenOutOfEu URL" in {
      validConfigService.customsDeclarationsGoodsTakenOutOfEuUrl must be(
        "https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
      )
    }

    "have serviceAvailability URL" in {
      validConfigService.serviceAvailabilityUrl must be(
        "https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
      )
    }

    // what is continue URL - redirect ?
    "have login continue URL" in {
      validConfigService.loginContinueUrl must be("http://localhost:9000/customs-declare-exports-frontend")
    }

    "have language translation enabled field" in {
      validConfigService.languageTranslationEnabled must be(false)
    }

    "have language map with English" in {
      validConfigService.languageMap.get("english").isDefined must be(true)
    }

    "have language map with Cymraeg" in {
      validConfigService.languageMap.get("cymraeg").isDefined must be(true)
    }

    "have default feature status" in {
      validConfigService.defaultFeatureStatus must be(FeatureStatus.disabled)
    }

    "return correct value for feature" in {
      validConfigService.featureStatus(Feature.default) must be(FeatureStatus.disabled)
    }

    "return correct value for isFeatureOn method" in {
      validConfigService.isFeatureOn(Feature.default) must be(false)
    }

    "have movements backend hostname " in {
      validConfigService.customsDeclareExportsMovements must be("http://localhost:9876")
    }

    "have movement Arrival submission URL" in {
      validConfigService.movementArrivalSubmissionUri must be("/movements/arrival")
    }

    "have movement Departure submission URL" in {
      validConfigService.movementDepartureSubmissionUri must be("/movements/departure")
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

    "have countryCodesJsonFilename" in {
      validConfigService.countryCodesJsonFilename must be("location-autocomplete-canonical-list.json")
    }

    "have countriesCsvFilename" in {
      validConfigService.countriesCsvFilename must be("code_lists/mdg-country-codes.csv")
    }

    "have nrsServiceUrl" in {
      validConfigService.nrsServiceUrl must be("http://localhostnrs:7654")
    }

    "have nrsApiKey" in {
      validConfigService.nrsApiKey must be("cds-exports")
    }

  }

  "throw an exception when google-analytics.host is missing" in {
    intercept[Exception](emptyConfigService.analyticsHost).getMessage must be(
      "Missing configuration key: google-analytics.host"
    )
  }

  "throw an exception when google-analytics.token is missing" in {
    intercept[Exception](emptyConfigService.analyticsToken).getMessage must be(
      "Missing configuration key: google-analytics.token"
    )
  }

  "throw an exception when auth.host is missing" in {
    intercept[Exception](emptyConfigService.authUrl).getMessage must be("Could not find config auth.host")
  }

  "throw an exception when urls.login is missing" in {
    intercept[Exception](emptyConfigService.loginUrl).getMessage must be("Missing configuration key: urls.login")
  }

  "throw an exception when urls.loginContinue is missing" in {
    intercept[Exception](emptyConfigService.loginContinueUrl).getMessage must be(
      "Missing configuration key: urls.loginContinue"
    )
  }

  "throw an exception when microservice.services.features.default is missing" in {
    intercept[Exception](emptyConfigService.defaultFeatureStatus).getMessage must be(
      "Missing configuration key: microservice.services.features.default"
    )
  }

  "throw an exception when customs-declare-exports-movements.host is missing" in {
    intercept[Exception](emptyConfigService.customsDeclareExportsMovements).getMessage must be(
      "Could not find config customs-declare-exports-movements.host"
    )
  }

  "throw an exception when movement Arrival submission uri is missing" in {
    intercept[Exception](emptyConfigService.movementArrivalSubmissionUri).getMessage must be(
      "Missing configuration for Customs Declarations Exports Movements Arrival submission URI"
    )
  }

  "throw an exception when movement Departure submission uri is missing" in {
    intercept[Exception](emptyConfigService.movementDepartureSubmissionUri).getMessage must be(
      "Missing configuration for Customs Declarations Exports Movements Departure submission URI"
    )
  }

  "throw an exception when consolidation submission uri is missing" in {
    intercept[Exception](emptyConfigService.movementConsolidationUri).getMessage must be(
      "Missing configuration for Customs Declarations Exports Movements Consolidation"
    )
  }

  "throw an exception when fetch all submissions uri is missing" in {
    intercept[Exception](emptyConfigService.fetchAllSubmissions).getMessage must be(
      "Missing configuration for Customs Declaration Exports fetch all submission URI"
    )
  }

  "throw an exception when fetch single submission uri is missing" in {
    intercept[Exception](emptyConfigService.fetchSingleSubmission).getMessage must be(
      "Missing configuration for Customs Declaration Exports fetch single submission URI"
    )
  }

  "throw an exception when fetch notifications uri is missing" in {
    intercept[Exception](emptyConfigService.fetchNotifications).getMessage must be(
      "Missing configuration for Customs Declarations Exports fetch notifications URI"
    )
  }

  "throw an exception when countryCodesJsonFilename is missing" in {
    intercept[Exception](emptyConfigService.countryCodesJsonFilename).getMessage must be(
      "Missing configuration key: countryCodesJsonFilename"
    )
  }

  "throw an exception when countryCodesCsvFilename is missing" in {
    intercept[Exception](emptyConfigService.countriesCsvFilename).getMessage must be(
      "Missing configuration key: countryCodesCsvFilename"
    )
  }

  "throw an exception when nrs.host is missing" in {
    intercept[Exception](emptyConfigService.nrsServiceUrl).getMessage must be("Could not find config nrs.host")
  }

  "throw an exception when nrs apikey is missing" in {
    intercept[Exception](emptyConfigService.nrsApiKey).getMessage must be("Missing configuration for nrs apikey")
  }

}
