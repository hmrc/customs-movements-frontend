/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import config.AppConfig
import controllers.routes
import models.SignedInUser
import models.requests.AuthenticatedRequest
import models.AuthKey.{enrolment, eoriIdentifierKey}
import play.api.{Configuration, Logging}
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject() (
  override val authConnector: AuthConnector,
  eoriAllowList: EoriAllowList,
  bodyParsers: PlayBodyParsers,
  appConfig: AppConfig
)(implicit override val executionContext: ExecutionContext)
    extends AuthAction with AuthorisedFunctions with Logging {

  override val parser: BodyParser[AnyContent] = bodyParsers.anyContent

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(Enrolment(enrolment))
      .retrieve(allEnrolments) { allEnrolments: Enrolments =>
        val allUserEnrolments = allEnrolments.enrolments

        val eori = allUserEnrolments
          .flatMap(_.getIdentifier(eoriIdentifierKey))
          .headOption

        validateEnrolments(eori)

        val cdsLoggedInUser = SignedInUser(eori.get.value, allEnrolments)

        val onAllowList = allowListAuthentication(cdsLoggedInUser.eori)

        if (onAllowList)
          block(new AuthenticatedRequest(request, cdsLoggedInUser))
        else {
          logger.warn(s"User rejected with onAllowList=$onAllowList")
          Future.successful(Results.Redirect(routes.UnauthorisedController.onPageLoad))
        }
      } recover {
      case s: NoActiveSession =>
        logger.warn(s"User rejected with ${s.getMessage}")
        Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
      case e: AuthorisationException =>
        logger.warn(s"User rejected with ${e.getMessage}")
        Results.Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private def validateEnrolments(eori: Option[EnrolmentIdentifier]): Unit =
    if (eori.isEmpty) {
      logger.info("Authentication Rejected: User doesn't have an EORI set")
      throw InsufficientEnrolments()
    }

  private def allowListAuthentication(eori: String): Boolean = {
    val eoriOnAllowList = eoriAllowList.allows(eori)

    if (!eoriOnAllowList)
      logger.info("Authentication Rejected: User's EORI not on allow list")

    eoriOnAllowList
  }

}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]

class EoriAllowList @Inject() (configuration: Configuration) {
  private val values = configuration.get[Seq[String]]("allowList.eori")
  def allows(eori: String): Boolean = values.isEmpty || values.contains(eori)
}
