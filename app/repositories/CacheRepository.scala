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

package repositories

import models.cache.Cache
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import repositories.CacheRepository.{eoriField, uuidField}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class CacheRepository @Inject() (mc: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Cache](mc, "cache", Cache.format, CacheRepository.indexes) with RepositoryOps[Cache] {

  override def classTag: ClassTag[Cache] = implicitly[ClassTag[Cache]]
  implicit val executionContext: ExecutionContext = ec

  def findByEoriAndAnswerCacheId(eori: String, answerCacheId: String): Future[Option[Cache]] =
    findAll(SearchParameters(eori = Some(eori), uuid = Some(answerCacheId))).map(_.headOption)(ec)

  def removeByEoriAndAnswerCacheId(eori: String, answerCacheId: String): Future[Unit] = removeEvery(eoriField, eori, uuidField, answerCacheId)

  def upsert(cache: Cache): Future[Cache] =
    findOneAndReplace(eoriField, cache.eori, uuidField, cache.uuid, cache)
}

object CacheRepository {
  private val eoriField = "eori"
  private val uuidField = "uuid"

  val indexes: Seq[IndexModel] = Seq(
    IndexModel(ascending(eoriField), IndexOptions().name("eoriIdx")),
    IndexModel(ascending("updated"), IndexOptions().name("ttl").expireAfter(3600, SECONDS))
  )
}
