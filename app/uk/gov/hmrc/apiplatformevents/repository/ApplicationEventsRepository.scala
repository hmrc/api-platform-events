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

import akka.stream.Materializer
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.{equal, size}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.libs.json.OFormat
import uk.gov.hmrc.apiplatformevents.models.MongoFormatters
import uk.gov.hmrc.apiplatformevents.models.common.{ApplicationEvent, EventType}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationEventsRepository @Inject()(mongoComponent: MongoComponent)
                                           (implicit ec: ExecutionContext, val mat: Materializer)
    extends PlayMongoRepository[ApplicationEvent](
      mongoComponent = mongoComponent,
      collectionName = "application-events",
     domainFormat = MongoFormatters.formatApplicationEvent,
      indexes = Seq(
        IndexModel(ascending("id"),
          IndexOptions()
            .name("id_index")
            .unique(true)
            .background(true))
      )

    ) with MongoJavatimeFormats.Implicits {


  def createEntity(event: ApplicationEvent): Future[Boolean] =
    collection.insertOne(event).toFuture().map(wr => wr.wasAcknowledged())


  def fetchEventsToNotify[A <: ApplicationEvent](eventType: EventType)(implicit formatter: OFormat[A]): Future[Seq[ApplicationEvent]] = {
      collection.aggregate(List(filter(equal("eventType", eventType)),
        lookup(from = "notifications", localField = "id", foreignField = "eventId", as = "notifcations"),
        filter(size("notifications", 0)))
      ).toFuture()
  }

  }
