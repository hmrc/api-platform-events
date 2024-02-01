package uk.gov.hmrc.apiplatformevents.support

import org.apache.pekko.util.Timeout
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import play.api.test.FutureAwaits
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.reflect.ClassTag

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
