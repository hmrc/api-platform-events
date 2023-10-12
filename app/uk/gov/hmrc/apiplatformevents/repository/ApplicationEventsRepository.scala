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

package uk.gov.hmrc.apiplatformevents.repository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.{equal, size}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.ApplicationEvent
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.EventsJsonFormatters
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import uk.gov.hmrc.apiplatformevents.models.Codecs

object MongoEventsJsonFormatters extends EventsJsonFormatters(MongoJavatimeFormats.instantFormat)

object ApplicationEventsRepository {
  lazy val formatter = MongoEventsJsonFormatters.abstractApplicationEventFormats
}

@Singleton
class ApplicationEventsRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[ApplicationEvent](
      mongoComponent = mongoComponent,
      collectionName = "application-events",
      domainFormat = ApplicationEventsRepository.formatter,
      indexes = Seq(
        IndexModel(
          ascending("id"),
          IndexOptions()
            .name("id_index")
            .unique(true)
            .background(true)
        ),
        IndexModel(
          ascending("eventType"),
          IndexOptions()
            .name("eventType_index")
            .unique(false)
            .background(true)
        ),
        IndexModel(
          ascending("applicationId"),
          IndexOptions()
            .name("applicationId_index")
            .unique(false)
            .background(true)
        )
      ),
      extraCodecs = Codecs.unionCodecs(ApplicationEventsRepository.formatter),
      replaceIndexes = true
    ) {

  def createEntity(event: ApplicationEvent): Future[Boolean] =
    collection.insertOne(event).toFuture().map(wr => wr.wasAcknowledged())

  def fetchEventsToNotify[A <: ApplicationEvent](): Future[List[ApplicationEvent]] = {
    collection
      .aggregate(
        Seq(
          filter(equal("eventType", "PPNS_CALLBACK_URI_UPDATED")),
          lookup(from = "notifications", localField = "id", foreignField = "eventId", as = "matched"),
          filter(size("matched", 0))
        )
      )
      .toFuture()
      .map(_.toList)
  }

  def fetchEvents(applicationId: ApplicationId): Future[List[ApplicationEvent]] = {
    collection
      .find(equal("applicationId", applicationId.value.toString()))
      .toFuture()
      .map(_.toList)
  }

  def deleteEventsForApplication(applicationId: ApplicationId): Future[Long] = {
    collection
      .deleteMany(equal("applicationId", Codecs.toBson(applicationId)))
      .toFuture()
      .map(dr => dr.getDeletedCount())
  }
}
