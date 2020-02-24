/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.Duration

import config.AppConfig
import javax.inject.Inject
import models.cache.IleQuery
import play.api.libs.json.JsString
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

class IleQueryRepository @Inject()(mc: ReactiveMongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends ReactiveRepository[IleQuery, BSONObjectID]("ileQueries", mc.mongoConnector.db, IleQuery.format, objectIdFormats) {

  override def indexes: Seq[Index] = super.indexes ++ Seq(
    Index(
      key = Seq("createdAt" -> IndexType.Ascending),
      name = Some("ttl"),
      options = BSONDocument("expireAfterSeconds" -> Duration.ofMinutes(1).getSeconds)
    )
  )

  def findBySessionIdAndUcr(sessionId: String, ucr: String): Future[Option[IleQuery]] =
    find("sessionId" -> sessionId, "ucr" -> ucr).map(_.headOption)

  def removeByConversationId(conversationId: String): Future[Unit] =
    remove("conversationId" -> JsString(conversationId)).map(_ => (): Unit)
}
