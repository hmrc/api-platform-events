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
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.AbstractApplicationEvent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId
import uk.gov.hmrc.apiplatformevents.models.Codecs
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.EventsJsonFormatters

object MongoEventsJsonFormatters extends EventsJsonFormatters(MongoJavatimeFormats.localDateTimeFormat)

object ApplicationEventsRepository {
  lazy val formatter = MongoEventsJsonFormatters.abstractApplicationEventFormats
}

@Singleton
class ApplicationEventsRepository @Inject()(mongoComponent: MongoComponent)
                                           (implicit ec: ExecutionContext)
    extends PlayMongoRepository[AbstractApplicationEvent](
      mongoComponent = mongoComponent,
      collectionName = "application-events",
     domainFormat = ApplicationEventsRepository.formatter,
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
            .background(true)),
        IndexModel(ascending("applicationId"),
          IndexOptions()
            .name("applicationId_index")
            .unique(false)
            .background(true))
      ),
      extraCodecs  = Codecs.unionCodecs(ApplicationEventsRepository.formatter),
      replaceIndexes = true
    ) {

  def createEntity(event: AbstractApplicationEvent): Future[Boolean] =
    collection.insertOne(event).toFuture().map(wr => wr.wasAcknowledged())

  def fetchEventsToNotify[A <: AbstractApplicationEvent](): Future[List[AbstractApplicationEvent]] = {
    collection.aggregate(
      Seq(
        filter(equal("eventType", "PPNS_CALLBACK_URI_UPDATED")),
        lookup(from = "notifications", localField = "id", foreignField = "eventId", as = "matched"),
        filter(size("matched", 0))
      )
    ).toFuture()
    .map(_.toList)
  }

  def fetchEvents(applicationId: ApplicationId): Future[List[AbstractApplicationEvent]] = {
    collection.find(equal("applicationId", applicationId.value.toString()))
    .toFuture()
    .map(_.toList)
  }
}