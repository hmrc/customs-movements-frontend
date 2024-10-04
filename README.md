
# customs-movements-frontend

This service is a frontend service for Exports Movements UI.
Its responsibility is to allow users submit Movements and Consolidations for their Export Declarations.

## Prerequisites
This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at a [JRE](https://www.java.com/en/download/) to run and a JDK for development.

This service uses MongoDB.

This service depends on other services. The easiest way to set up required microservices is to use Service Manager and profiles from [service-manager-config](https://github.com/hmrc/service-manager-config/) repository:
- CDS_EXPORTS_MOVEMENTS_ALL - all services together with both declarations services

### Running the application
In order to run the application you need to have SBT installed. Then, it is enough to start the service with:

`sbt run`

### Testing the application
This repository contains unit tests for the service. In order to run them, simply execute:

`sbt test`

## Developer notes

### Feature flags
This service uses feature flags to enable/disable some of its features. These can be changed/overridden in config under `microservice.services.features.<featureName>` key.

The list of feature flags and what they are responsible for:

`betaBanner = [enabled/disabled]` - When enabled, all pages in the service have BETA banner.

`tdrUnauthorisedMessage = [enabled/disabled]` - When enabled, the unauthorised error page will display TDR specific content.

### Allow lists
This service has two configurable allow lists that follow the standard behaviour of it list is empty then everyone is allowed, if it is populated with one or more values then only those values are allowed:

* allowList - the standard allow list that controls which EORIs are allowed to access the service.
* arriveDepartAllowList - a customs allow list that controls which EORIs can see/access the movement (arrive & depart) journey types.

### Auto Complete

This project has a 
[TamperMonkey](https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo?hl=en) (Google Chrome)
or 
[GreaseMonkey](https://addons.mozilla.org/en-GB/firefox/addon/greasemonkey/) (Firefox)
Auto Complete Script to help speed through the form journey.

These scripts can be found in the docs directory.

## ILE Query

A flow diagram for ILE Query is available on [Confluence](https://confluence.tools.tax.service.gov.uk/display/CD/ILE+Query+flow+diagram).

## TDRSecret values for a given EORI
As this service deployed in ExternalTest as part of the CDS Trader Dress Rehearsal, an additional enrolment value of 'TDRSecret' is required for a user to successfully authenticate in this environment.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").