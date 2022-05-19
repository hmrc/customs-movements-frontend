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

package repositories

import models.cache.Cache
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class CacheRepository @Inject()(mc: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Cache](mc, "cache", Cache.format, CacheRepository.indexes) with RepositoryOps[Cache] {

  override def classTag: ClassTag[Cache] = implicitly[ClassTag[Cache]]
  implicit val executionContext = ec

  def findByEori(eori: String): Future[Option[Cache]] = findOne("eori", eori)

  def removeByEori(eori: String): Future[Unit] = removeEvery("eori", eori)

  def upsert(cache: Cache): Future[Cache] =
    findOneAndReplace("eori", cache.eori, cache)

}

object CacheRepository {
  val indexes: Seq[IndexModel] = Seq(
    IndexModel(ascending("eori"), IndexOptions().name("eoriIdx")),
    IndexModel(ascending("updated"), IndexOptions().name("ttl").expireAfter(3600, SECONDS))
  )
}
