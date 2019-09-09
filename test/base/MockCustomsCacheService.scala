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

package base

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.ACCEPTED
import services.CustomsCacheService
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait MockCustomsCacheService extends MockitoSugar with BeforeAndAfterEach { self: Suite =>

  val mockCustomsCacheService: CustomsCacheService = mock[CustomsCacheService]

  def withCaching[T](formId: String, dateToReturn: Option[T]): OngoingStubbing[Future[Option[T]]] =
    when(mockCustomsCacheService.fetchAndGetEntry[T](any(), ArgumentMatchers.eq(formId))(any(), any(), any()))
      .thenReturn(Future.successful(dateToReturn))

  def withCacheMap(dateToReturn: Option[CacheMap]): OngoingStubbing[Future[Option[CacheMap]]] =
    when(mockCustomsCacheService.fetch(any())(any(), any()))
      .thenReturn(Future.successful(dateToReturn))

  def withCaching(formId: String): OngoingStubbing[Future[CacheMap]] =
    when(
      mockCustomsCacheService
        .cache(any(), ArgumentMatchers.eq(formId), any())(any(), any(), any())
    ).thenReturn(Future.successful(CacheMap("", Map.empty)))

  def mockCustomsCacheServiceClearedSuccessfully(): OngoingStubbing[Future[HttpResponse]] =
    when(mockCustomsCacheService.remove(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  override protected def afterEach(): Unit = {
    reset(mockCustomsCacheService)
    super.afterEach()
  }
}
