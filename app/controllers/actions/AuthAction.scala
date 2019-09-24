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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject, ProvidedBy}
import controllers.routes
import javax.inject.Provider
import models.SignedInUser
import models.requests.AuthenticatedRequest
import play.api.Configuration
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.{NoActiveSession, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(
  override val authConnector: AuthConnector,
  eoriWhitelist: EoriWhitelist,
  mcc: MessagesControllerComponents
) extends AuthAction with AuthorisedFunctions {

  implicit override val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(Enrolment("HMRC-CUS-ORG"))
      .retrieve(allEnrolments) {
        case allEnrolments =>
          val eori = allEnrolments
            .getEnrolment("HMRC-CUS-ORG")
            .flatMap(_.getIdentifier("EORINumber"))

          if (eori.isEmpty) {
            throw InsufficientEnrolments()
          }

          val cdsLoggedInUser = SignedInUser(eori.get.value, allEnrolments)

          if (eoriWhitelist.contains(cdsLoggedInUser)) {
            block(AuthenticatedRequest(request, cdsLoggedInUser))
          } else {
            Future.successful(Results.Redirect(routes.UnauthorisedController.onPageLoad()))
          }
      }
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction
    extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]

case class NoExternalId() extends NoActiveSession("No externalId was found")

@ProvidedBy(classOf[EoriWhitelistProvider])
class EoriWhitelist(values: Seq[String]) {
  def contains(user: SignedInUser): Boolean = values.isEmpty || values.contains(user.eori)
}

class EoriWhitelistProvider @Inject()(configuration: Configuration) extends Provider[EoriWhitelist] {
  override def get(): EoriWhitelist =
    new EoriWhitelist(configuration.get[Seq[String]]("whitelist.eori"))
}
