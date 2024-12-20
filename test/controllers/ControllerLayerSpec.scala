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

package controllers

import base.{MockAuthConnector, MockNavigator, UnitSpec}
import controllers.actions._
import forms.DucrPartChiefChoice
import models.cache.JourneyType.JourneyType
import models.cache.{Answers, Cache}
import models.requests.{AuthenticatedRequest, JourneyRequest, SessionHelper}
import models.{SignedInUser, UcrBlock}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.libs.json.Writes
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.CacheRepository
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolments}
import utils.Stubs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class ControllerLayerSpec extends UnitSpec with BeforeAndAfterEach with CSRFSupport with Stubs with MockNavigator with MockAuthConnector {

  protected val user = SignedInUser("eori", Enrolments(Set.empty))
  protected def getRequest(): Request[AnyContent] = FakeRequest(GET, "/").withCSRFToken

  protected def getRequest(cache: Cache) = FakeRequest().withSession(SessionHelper.ANSWER_CACHE_ID -> cache.uuid).withCSRFToken
  protected def postRequest[T](body: T, cache: Cache)(implicit wts: Writes[T]): Request[AnyContentAsJson] =
    FakeRequest("POST", "/")
      .withSession(SessionHelper.ANSWER_CACHE_ID -> cache.uuid)
      .withJsonBody(wts.writes(body))
      .withCSRFToken

  protected def postRequest(): Request[AnyContent] = FakeRequest(POST, "/").withCSRFToken
  protected def postRequest[T](body: T)(implicit wts: Writes[T]): Request[AnyContentAsJson] =
    FakeRequest("POST", "/").withJsonBody(wts.writes(body)).withCSRFToken

  protected def postRequestAsFormUrlEncoded(body: (String, String)*): Request[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/")
      .withFormUrlEncodedBody(body: _*)
      .withCSRFToken

  protected implicit def messages(implicit request: Request[_]): Messages = stubMessagesControllerComponents().messagesApi.preferred(request)
  protected implicit val flashApi: Flash = Flash()

  protected def contentAsHtml(of: Future[Result]): Html = Html(contentAsBytes(of).decodeString(charset(of).getOrElse("utf-8")))

  case class SuccessfulAuth(operator: SignedInUser = user)
      extends AuthActionImpl(mock[AuthConnector], mock[EoriAllowList], stubMessagesControllerComponents().parsers, appConfig) {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, operator))
  }

  case object UnsuccessfulAuth
      extends AuthActionImpl(mock[AuthConnector], mock[EoriAllowList], stubMessagesControllerComponents().parsers, appConfig) {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      Future.successful(Results.Forbidden)
  }

  case class ValidJourney(
    answers: Answers,
    ucrBlock: Option[UcrBlock] = None,
    ucrBlockFromIleQuery: Boolean = false,
    ducrPartChiefChoice: Option[DucrPartChiefChoice] = None
  ) extends JourneyRefiner(mock[CacheRepository], mock[ArriveDepartAllowList]) {

    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
      val cache = Cache(request.eori, Some(answers), ucrBlock, ucrBlockFromIleQuery, ducrPartChiefChoice)
      Future.successful(Right(JourneyRequest(cache, request)))
    }

    override def apply(types: JourneyType*): ActionRefiner[AuthenticatedRequest, JourneyRequest] =
      if (!types.contains(answers.`type`)) InValidJourney
      else ValidJourney(answers, ucrBlock, ucrBlockFromIleQuery, ducrPartChiefChoice)
  }

  case object InValidJourney extends JourneyRefiner(mock[CacheRepository], mock[ArriveDepartAllowList]) {
    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
      Future.successful(Left(Results.Forbidden))
  }
}
