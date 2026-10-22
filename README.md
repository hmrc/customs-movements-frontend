
# customs-movements-frontend

## About
This public-facing microservice is part of the Customs Exports Declaration Service (CEDS). It is designed to work in tandem with the [customs-declare-exports-movements](https://github.com/hmrc/customs-declare-exports-movements) back-end service.

Its responsibility is to allow users submit Movements and Consolidations for their Export Declarations.

| Key                             | Value                                                                                                        | 
|---------------------------------|--------------------------------------------------------------------------------------------------------------|
| Digital service                 | CDS Exports                                                                                                  |
| Local port                      | `6796`                                                                                                       |
| Base path                       | `/customs-movements`                                                                                         |
| Back-end                        | [customs-declare-exports-movements](https://github.com/hmrc/customs-declare-exports-movements) (port `6797`) |
| Internal Front-end              | [customs-exports-internal-frontend](https://github.com/hmrc/customs-exports-internal-frontend) (port `6796`) |
| Acceptance tests                | [movements-ui-acceptance-tests](https://github.com/hmrc/movements-ui-acceptance-tests)                       |
| Internal Acceptance tests       | [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests)     |
| Performance tests               | [cds-movements-performance-tests](https://github.com/hmrc/cds-movements-performance-tests)                   |
| Stubs used in Local and Staging | [customs-inventory-linking-exports-stub](https://github.com/hmrc/customs-inventory-linking-exports-stub)     |

## How to Run This Service

### Prerequisites
Use [Service Manager](https://github.com/hmrc/customs-movements-frontend#service-manager-profiles) to start all the services that CDS Exports depends on:

```bash
sm2 --start CDS_EXPORTS_MOVEMENTS_ALL
```

### Running the Service Locally
To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt:
```bash
sm2 --stop CUSTOMS_MOVEMENTS_FRONTEND
sbt run
```
The service starts on port `6796`. To access it, https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in, using the [enrolment details](#enrolment-required) below and you will be redirected to http://localhost:6796/customs-movements/choice.

## How to Test This Service

### Local

#### Unit Tests
```bash
sbt test
```

#### Pre-Push Check
There is a script called `precheck.sh` that runs all tests, examines their coverage and checks if all the files are properly formatted.
It is good practice to run it just before pushing to GitHub.

```bash
./precheck.sh
```

#### Acceptance Tests (Smoke and Regression)
The acceptance tests for this service are in [movements-ui-acceptance-tests](https://github.com/hmrc/movements-ui-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/movements-ui-acceptance-tests#how-to-run-the-tests).

The internal UI acceptance tests for this service are in [movements-internal-ui-acceptance-tests](https://github.com/hmrc/movements-internal-ui-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/movements-internal-ui-acceptance-tests#how-to-run-the-tests).

#### Performance tests
The performance tests for this service are in [cds-movements-performance-tests](https://github.com/hmrc/cds-movements-performance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/cds-movements-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging
After the successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-movements-frontend-pipeline/), manual testing is performed in the QA and/or Staging environments, see [Manual Service Verification in QA and Staging](https://github.com/hmrc/movements-ui-acceptance-tests#manual-testing-in-qa-and-staging).

## Service Catalogue
- [customs-movements-frontend in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-movements-frontend)

## Jenkins Pipeline
- Front End Build and Deployment Pipeline
    -  [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-movements-frontend/)
    -  [Deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-movements-frontend-pipeline/)
- The acceptance test Jenkins jobs (smoke and regression) are listed in the [movements-ui-acceptance-tests README](https://github.com/hmrc/movements-ui-acceptance-tests#how-to-run-tests).
- The performance test Jenkins jobs are listed in the [cds-movements-performance-tests README](https://github.com/hmrc/cds-movements-performance-tests/#jenkins-builds).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile                       | Use                                                                                                      | Services started                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|-------------------------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CDS_EXPORTS_MOVEMENTS_ALL`   | Running and developing this service locally                                                              | `CUSTOMS_DECLARE_EXPORTS_MOVEMENTS`, `CUSTOMS_MOVEMENTS_FRONTEND`, `AUTH`, `AUTH_LOGIN_API`, `CENTRALISED_AUTHORISATION_SERVER`, `AUTH_LOGIN_STUB`, `IDENTITY_VERIFICATION`, `API_SUBSCRIPTION_FIELDS`, `CUSTOMS_INVENTORY_LINKING_EXPORTS`, `CUSTOMS_INVENTORY_LINKING_EXPORTS_STUB`, `CUSTOMS_NOTIFICATION`, `CUSTOMS_NOTIFICATIONS_RECEIVER_STUB`, `CUSTOMS_NOTIFICATION_GATEWAY`, `API_NOTIFICATION_QUEUE`, `USER_DETAILS`, `CONTACT_FRONTEND`, `BAS_GATEWAY`, `BAS_GATEWAY_FRONTEND`, `STRIDE_AUTH`, `STRIDE_AUTH_FRONTEND`, `STRIDE_IDP_STUB`, `CUSTOMS_EXPORTS_INTERNAL_FRONTEND` |
| `CDS_EXPORTS_MOVEMENTS_ATS`   | Running the [acceptance tests](https://github.com/hmrc/exports-ui-acceptance-tests#how-to-run-the-tests) | `CUSTOMS_DECLARE_EXPORTS_MOVEMENTS`, `CUSTOMS_MOVEMENTS_FRONTEND`, `AUTH`, `AUTH_LOGIN_API`, `AUTH_LOGIN_STUB`, `IDENTITY_VERIFICATION`, `CUSTOMS_INVENTORY_LINKING_EXPORTS_STUB`, `USER_DETAILS`, `BAS_GATEWAY`, `BAS_GATEWAY_FRONTEND`                                                                                                                                                                                                                                                                                                                                                 |
| `CDS_EXPORTS_ALL`             | All CDS Exports services (declarations, movements and internal)                                          | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |

| Service                                    | Port | Repository                                                                     |
|--------------------------------------------|------|--------------------------------------------------------------------------------|
| `CUSTOMS_MOVEMENTS_FRONTEND`               | 6796 | This service                                                                   |
| `CUSTOMS_DECLARE_EXPORTS_MOVEMENTS`        | 6797 | [customs-declare-exports-movements](https://github.com/hmrc/customs-declare-exports-movements)                                          |
| `CUSTOMS_DECLARE_EXPORTS`                  | 6792 | [customs-declare-exports](https://github.com/hmrc/customs-declare-exports)     |
| `CUSTOMS_DECLARATIONS_STUB`                | 6790 | [customs-declarations-stub](https://github.com/hmrc/customs-declarations-stub) |

## Endpoints
| Method   | Path                             | Purpose                                                                                    | Sample request (query / form body)        | Response                                                                                                                                        |
|----------|----------------------------------|--------------------------------------------------------------------------------------------|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| GET      | `/`                              | Service start / root page                                                                  | -                                         | `303` → `/choice`                                                                                                                               |
| GET      | `/choice`                        | Landing page, display available choices to manage customs movements for exports            | -                                         | `200`                                                                                                                                           |
| GET      | `/choice/:choice`                | Submit selected choice, redirect to relavant page                                          | /choice/:choice    (choice: String)       | `303` → `/consignment-query`, `/consignment-references`, `/mucr-options`, `/disassociate-ucr`, `/shut-mucr`, `/movements`                       |
| GET      | `/choice-on-consignment`         | Display choices for what to do with a selected consignment                                 | -                                         | `200`                                                                                                                                           |
| POST     | `/choice-on-consignment`         | Submit choice for a selected consignment                                                   | choice: String                            | `400` `303` → `/specific-date-and-time`, `/disassociate-ucr-summary`, `/shut-mucr-summary `, `/manage-mucr`, `/choice-on-consignment`, `/choice` |
| GET      | `/consignment-query`             | Display find consignment search page                                                       | -                                         | `200`                                                                                                                                           |
| POST     | `/consignment-query`             | Search for consignment by MUCR/DUCR                                                        | ucr: String                               | `400` `303` → `/consignment-query/:ucr`,                                                                                                        |
| GET      | `/consignment-query/:ucr`        | Displays details for consignment                                                           | /consignment-query/:ucr                   | `200`                                                                                                                                           |
| GET      | `/mucr-options`                  | Display MUCR options                                                                       | -                                         | `200`                                                                                                                                           |
| POST     | `/mucr-options`                  | Save selected MUCR option and redirect to continue journey                                 | form: MucrOptions                         | `400` `303` → `/associate-ucr-summary`, `/associate-ucr`                                                                                        |
| GET      | `/manage-mucr`                   | Display manage MUCR page                                                                   | -                                         | `200`, `500` `303` → `/mucr-options`                                                                                                                                   |
| POST     | `/manage-mucr`                   | Submit manage MUCR request                                                                 | -                                         | `400` `303` → `/mucr-options`, `/associate-ucr`                                                                                                                                                |
| GET      | `/associate-ucr`                 | Display associate UCR page to add new consignment by MUCR/DUCR                             | -                                         | `200`                                                                                                                                           |
| POST     | `/associate-ucr`                 | Submit associate UCR request to add new consignment by MUCR/DUCR                           | form: AssociateUcr                        | `400` `303` → `/associate-ucr-summary`                                                                                                          |
| GET      | `/disassociate-ucr`              | Display disassociate UCR page to remove new consignment by MUCR/DUCR                       | -                                         | `200`                                                                                                                                           |
| POST     | `/disassociate-ucr`              | Submit disassociate UCR request to remove new consignment by MUCR/DUCR                     | form: DisassociateUcr                     | `400` `303` → `/disassociate-ucr-summary`                                                                                                       |
| GET      | `/consignment-references`        | Enter consignment reference (DUCR/MUCR)                                                    | -                                         | `200`                                                                                                                                           |
| POST     | `/consignment-references`        | Save consignment reference to cache and redirect                                           | form: ConsignmentReferences               | `400` `303` → `/specific-date-and-time`                                                                                                         |
| GET      | `/movement-details`              | Display movement details page - enter date and time of arrival/departure                   | -                                         | `200`                                                                                                                                           |
| POST     | `/movement-details`              | Save movement details (date and time of arrival/departure) and redirect to location page   | form: ArrivalDetails or DepartureDetails  | `400` `303` → `/location`                                                                                                                       |
| GET      | `/specific-date-and-time`        | Display specific date and time page                                                        | -                                         | `200`                                                                                                                                           |
| POST     | `/specific-date-and-time`        | Submit specific date and time and redirect to continue journey                             | form: SpecificDateTimeChoice              | `400` `303` → `/movement-details`, `/location`                                                                                                  |
| GET      | `/location`                      | Display location page and redirect to continue journey                                     | -                                         | `200`                                                                                                                                           |
| POST     | `/location`                      | Save location                                                                              | form: Location                            | `400` `303` → `/summary`, `/transport`                                                                                                          |
| GET      | `/transport`                     | Display transport details page                                                             | -                                         | `200`                                                                                                                                           |
| POST     | `/transport`                     | Save transport details and redirect to summary                                             | form: Transport                           | `400` `303` → `/summary`                                                                                                                        |
| GET      | `/shut-mucr`                     | Display shut MUCR page                                                                     | -                                         | `200`                                                                                                                                           |
| POST     | `/shut-mucr`                     | Submit shut MUCR request                                                                   | form: ShutMucr                            | `400` `303` → `/shut-mucr-summary`                                                                                                              |
| GET      | `/summary`                       | Display arrive/depart summary page                                                         | -                                         | `200`                                                                                                                                           |
| POST     | `/summary`                       | Submit arrive/depart summary form and redirect to confirmation                             | -                                         | `303` → `/movement-confirmation`                                                                                                                |
| GET      | `/associate-ucr-summary`         | Display associate UCR summary to check answers before submitting association request       | -                                         | `200`                                                                                                                                           |
| POST     | `/associate-ucr-summary`         | Submit association request                                                                 | -                                         | `303` → `/movement-confirmation`                                                                                                                |
| GET      | `/disassociate-ucr-summary`      | Display disassociate UCR summary to check answers before submitting disassociation request | -                                         | `200`                                                                                                                                           |
| POST     | `/disassociate-ucr-summary`      | Submit disassociate UCR summary                                                            | -                                         | `303` → `/movement-confirmation`                                                                                                                |
| GET      | `/shut-mucr-summary`             | Display shut MUCR summary                                                                  | -                                         | `200`                                                                                                                                           |
| POST     | `/shut-mucr-summary`             | Submit shut MUCR summary                                                                   | -                                         | `303` → `/movement-confirmation`                                                                                                                |
| GET      | `/movement-confirmation`         | Display movement confirmation page                                                         | -                                         | `200`                                                                                                                                           |
| GET      | `/movements`                     | Display all submitted movements assosiated with EORI                                       | -                                         | `200`                                                                                                                                           |
| GET      | `/notifications/:conversationId` | Display notifications for a conversation                                                   | /:conversationId (conversationId: String) | `200`, `500`, `303` → `//movements`                                                                                                             |
| GET      | `/sign-out`                      | Sign out user with reason                                                                  | signOutReason: SignOutReason              | `303` → `/we-signed-you-out`, `/you-have-signed-out`                                                                                            |
| GET      | `/we-signed-you-out`             | Session timeout sign-out page                                                              | -                                         | `200`                                                                                                                                           |
| GET      | `/you-have-signed-out`           | User sign-out confirmation page                                                            | -                                         | `200`                                                                                                                                           |

## Enrolment Required

Locally, sign in through the auth login stub at http://localhost:9949/auth-login-stub/gg-sign-in, enter the values below, and submit.

| Field            | Value                                                                                                                                 |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Redirect URL     | Local: `http://localhost:6796/customs-movements/choice`<br>Staging: `https://www.staging.tax.service.gov.uk/customs-movements/choice` |
| Enrolment Key    | `HMRC-CUS-ORG`                                                                                                                        |
| Identifier Name  | `EORINumber`                                                                                                                          |
| Identifier Value | A GB EORI number, e.g. `GB123456789006`                                                                                               |

## Developer notes

### Feature flags
This service uses feature flags to enable/disable some of its features. These can be changed/overridden in config under `microservice.services.features.<featureName>` key.

The list of feature flags and what they are responsible for:

`betaBanner = [enabled/disabled]` - When enabled, all pages in the service have BETA banner.

### Allow lists
This service has two configurable allow lists that follow the standard behaviour of it list is empty then everyone is allowed, if it is populated with one or more values then only those values are allowed:

* allowList - the standard allow list that controls which EORIs are allowed to access the service.
* arriveDepartAllowList - a customs allow list that controls which EORIs can see/access the movement (arrive & depart) journey types.

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

### Autocomplete
This project has a
[TamperMonkey](https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo?hl=en) (Google Chrome)
or
[GreaseMonkey](https://addons.mozilla.org/en-GB/firefox/addon/greasemonkey/) (Firefox)
`autocomplete` script to help you get through the form journey faster.

You can find these scripts in the [docs](https://github.com/hmrc/customs-declare-exports-frontend/tree/main/docs) directory.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").