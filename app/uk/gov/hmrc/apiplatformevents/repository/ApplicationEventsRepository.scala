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

package uk.gov.hmrc.apiplatformevents.repository

import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.{equal, size}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.apiplatformevents.models.MongoFormatters._
import uk.gov.hmrc.apiplatformevents.models.{ApplicationEvent, MongoFormatters}
import uk.gov.hmrc.apiplatformevents.models.common.EventType
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.apiplatformevents.models.Codecs
import org.bson.conversions.Bson

@Singleton
class ApplicationEventsRepository @Inject()(mongoComponent: MongoComponent)
                                           (implicit ec: ExecutionContext)
    extends PlayMongoRepository[ApplicationEvent](
      mongoComponent = mongoComponent,
      collectionName = "application-events",
     domainFormat = MongoFormatters.formatApplicationEvent,
      indexes = Seq(
        IndexModel(ascending("id"),
          IndexOptions()
            .name("id_index")
            .unique(true)
            .background(true)),
        IndexModel(ascending("eventType"),
          IndexOptions()
            .name("eventType_index")
            .unique(false)
            .background(true))
      ),
      extraCodecs  = Codecs.unionCodecs[ApplicationEvent](formatApplicationEvent),
      replaceIndexes = true
    ) {

  def createEntity(event: ApplicationEvent): Future[Boolean] =
    collection.insertOne(event).toFuture().map(wr => wr.wasAcknowledged())

  def fetchEventsToNotify[A <: ApplicationEvent](eventType: EventType): Future[Seq[ApplicationEvent]] = {
    collection.aggregate(
      Seq(
        filter(equal("eventType", eventType.entryName)),
        lookup(from = "notifications", localField = "id", foreignField = "eventId", as = "matched"),
        filter(size("matched", 0))
      )
    ).toFuture()
  }

  def fetchEventsBy(applicationId: String, eventType: Option[EventType]): Future[Seq[ApplicationEvent]] = {
    val filters = Seq(filter(equal("applicationId", applicationId))) ++ (eventType.fold(Seq.empty[Bson])(et => Seq(filter(equal("eventType", et.entryName)))))

    collection.aggregate(filters)
    .toFuture()
  }
}