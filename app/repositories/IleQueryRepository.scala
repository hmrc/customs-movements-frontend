/*
 * Copyright 2023 HM Revenue & Customs
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

import com.mongodb.client.model.Indexes.ascending
import config.AppConfig
import models.cache.IleQuery
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class IleQueryRepository @Inject() (mongo: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IleQuery](mongo, "ileQueries", IleQuery.format, IleQueryRepository.indexes) with RepositoryOps[IleQuery] {

  override def classTag: ClassTag[IleQuery] = implicitly[ClassTag[IleQuery]]
  implicit val executionContext: ExecutionContext = ec

  def findBySessionIdAndUcr(sessionId: String, ucr: String): Future[Option[IleQuery]] = {
    val query = Json.obj("sessionId" -> sessionId, "ucr" -> ucr)
    collection.find(BsonDocument(query.toString)).headOption()
  }

  def removeByConversationId(conversationId: String): Future[Unit] =
    removeEvery("conversationId", conversationId)
}

object IleQueryRepository {
  val indexes: Seq[IndexModel] = Seq(IndexModel(ascending("createdAt"), IndexOptions().name("ttl").expireAfter(60, SECONDS)))
}
