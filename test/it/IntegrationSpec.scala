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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import connectors.{AuditWiremockTestServer, AuthWiremockTestServer, MovementsBackendWiremockTestServer}
import models.cache.{Answers, Cache}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request, Result}
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeRequest}
import play.api.{Application, Logger}
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONCollection
import repositories.CacheRepository
import repository.TestMongoDB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class IntegrationSpec
    extends WordSpec with MustMatchers with BeforeAndAfterEach with GuiceOneServerPerSuite with AuthWiremockTestServer
    with MovementsBackendWiremockTestServer with AuditWiremockTestServer with Eventually with TestMongoDB {

  /*
    Intentionally NOT exposing the real CacheRepository as we shouldn't test our production code using our production classes.
   */
  private lazy val cacheRepository: JSONCollection = app.injector.instanceOf[CacheRepository].collection

  override lazy val port = 14681
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(authConfiguration)
      .configure(movementsBackendConfiguration)
      .configure(mongoConfiguration)
      .configure(auditConfiguration)
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(cacheRepository.drop(failIfNotFound = false))
  }

  protected def get(call: Call): Future[Result] =
    route(app, FakeRequest("GET", call.url)).get

  protected def post[T](call: Call, payload: (String, String)*): Future[Result] = {
    val request: Request[AnyContentAsFormUrlEncoded] = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", call.url).withFormUrlEncodedBody(payload: _*))
    route(app, request).get
  }

//  protected def theCacheFor(eori: String): Option[Answers] = await(cacheRepository.find(Json.obj("eori" -> eori)).one[Cache]).flatMap(_.answers)
  protected def theCacheFor(eori: String): Option[Cache] = await(cacheRepository.find(Json.obj("eori" -> eori)).one[Cache])

  protected def theAnswersFor(eori: String): Option[Answers] = theCacheFor(eori).flatMap(_.answers)

  protected def givenCacheFor(eori: String, answers: Answers): Unit = await(cacheRepository.insert(Cache.format.writes(Cache(eori, answers))))

  protected def verifyEventually(requestPatternBuilder: RequestPatternBuilder): Unit = eventually(WireMock.verify(requestPatternBuilder))

}
