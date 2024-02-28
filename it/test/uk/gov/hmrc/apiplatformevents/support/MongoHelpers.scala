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

package uk.gov.hmrc.apiplatformevents.support

import scala.reflect.ClassTag

import org.apache.pekko.util.Timeout
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}

import play.api.test.FutureAwaits
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

trait MongoHelpers { _: FutureAwaits =>

  def count[A](repo: => PlayMongoRepository[A])(implicit timeout: Timeout): Long =
    await(repo.collection.countDocuments().toFuture())

  def removeAll[A](repo: => PlayMongoRepository[A])(implicit timeout: Timeout): DeleteResult =
    await(repo.collection.deleteMany(BsonDocument()).toFuture())

  def insert[A](repo: => PlayMongoRepository[A], model: A)(implicit timeout: Timeout): InsertOneResult =
    await(repo.collection.insertOne(model).toFuture())

  def findAll[A](repo: => PlayMongoRepository[A])(implicit timeout: Timeout, classTag: ClassTag[A]): Seq[A] =
    await(repo.collection.find().toFuture())
}
