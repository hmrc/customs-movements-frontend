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

import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.mongodb.client.model.ReturnDocument
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{FindOneAndReplaceOptions, FindOneAndUpdateOptions}
import org.mongodb.scala.{Document, MongoCollection, MongoWriteException}
import play.api.Logging
import play.api.libs.json.{Json, Writes}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait RepositoryOps[T] extends Logging {

  implicit def classTag: ClassTag[T]
  implicit val executionContext: ExecutionContext

  val collection: MongoCollection[T]

  def findAll: Future[Seq[T]] =
    collection.find().toFuture()

  def findAll[V](keyId: String, keyValue: V): Future[Seq[T]] =
    collection.find(equal(keyId, keyValue)).toFuture()

  def findAll(searchParameters: SearchParameters): Future[Seq[T]] =
    if (searchParameters.isEmpty) Future.successful(Seq.empty)
    else {
      val query = BsonDocument(Json.toJson(searchParameters).toString)
      collection.find(query).toFuture()
    }

  def findOne[V](keyId: String, keyValue: V): Future[Option[T]] =
    collection.find(equal(keyId, keyValue)).toFuture().map(_.headOption)

  /*
   Find one and return if a document with keyId=keyValue exists,
   or create "document: T" if a document with keyId=keyValue does NOT exists.
   */
  def findOneOrCreate[V](keyId: String, keyValue: V, document: => T)(implicit writes: Writes[T]): Future[T] =
    collection
      .findOneAndUpdate(
        filter = equal(keyId, keyValue),
        update = BsonDocument(Json.toJson(document).toString()),
        options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()

  /*
   Find one and replace with "document: T" if a document with keyId=keyValue exists,
   or create "document: T" if a document with keyId=keyValue does NOT exists.
   */
  def findOneAndReplace[V](keyId: String, keyValue: V, document: T): Future[T] =
    collection
      .findOneAndReplace(
        filter = equal(keyId, keyValue),
        replacement = document,
        options = FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()

  /*
   Find one and replace with "document: T" only if a document with keyId=keyValue exists,
   */
  def findOneAndReplaceIfExists[V](keyId: String, keyValue: V, document: T): Future[Option[T]] =
    collection
      .findOneAndReplace(
        filter = equal(keyId, keyValue),
        replacement = document,
        options = FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
      )
      .toFutureOption()

  def findOneAndRemove[V](keyId: String, keyValue: V): Future[Option[T]] =
    collection.findOneAndDelete(equal(keyId, keyValue)).toFutureOption()

  def indexList: Future[Seq[Document]] = collection.listIndexes().toFuture()

  def insertOne(document: T): Future[Either[WriteError, T]] =
    collection
      .insertOne(document)
      .toFuture()
      .map(_ => Right(document))
      .recover {
        case exc: MongoWriteException if exc.getError.getCategory == DUPLICATE_KEY =>
          Left(DuplicateKey(exc.getError.getMessage))
      }

  def removeAll: Future[Unit] =
    collection.deleteMany(BsonDocument()).toFuture().map(_ => ())

  def removeEvery[V](keyId: String, keyValue: V): Future[Unit] =
    collection.deleteMany(equal(keyId, keyValue)).toFuture().map(_ => ())

  def removeOne[V](keyId: String, keyValue: V): Future[Unit] =
    collection.deleteOne(equal(keyId, keyValue)).toFuture().map(_ => ())

  def size: Future[Long] = collection.countDocuments().toFuture()
}

sealed abstract class WriteError(message: String)

case class GenericError(message: String) extends WriteError(message)
case class DuplicateKey(message: String) extends WriteError(message)
