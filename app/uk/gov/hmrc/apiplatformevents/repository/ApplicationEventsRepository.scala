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
import akka.stream.scaladsl.Source
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.apiplatformevents.models.ReactiveMongoFormatters
import uk.gov.hmrc.apiplatformevents.models.common.{ApplicationEvent, EventType}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationEventsRepository @Inject()(mongoComponent: ReactiveMongoComponent)
                                           (implicit ec: ExecutionContext, val mat: Materializer)
    extends ReactiveRepository[ApplicationEvent, BSONObjectID](
      "application-events",
      mongoComponent.mongoConnector.db,
      ReactiveMongoFormatters.formatApplicationEvent,
      ReactiveMongoFormats.objectIdFormats) {

  override def indexes = Seq(
    Index(
      key = Seq("id" -> IndexType.Ascending),
      name = Some("id_index"),
      unique = true,
      background = true
    )
  )

  def createEntity(event: ApplicationEvent): Future[Boolean] =
    insert(event).map(wr => wr.ok)

  def fetchEventsToNotify[A <: ApplicationEvent](eventType: EventType)(implicit formatter: OFormat[A]): Source[A, Future[Any]] = {
    val builder = collection.BatchCommands.AggregationFramework
    val pipeline = List(
      builder.Match(Json.obj("eventType" -> eventType)),
      builder.Lookup(from = "notifications", localField = "id", foreignField = "eventId", as = "notifications"),
      builder.Match(Json.obj("notifications" -> Json.obj("$size" -> 0)))
    )
    collection.aggregateWith[A]()(_ => (pipeline.head, pipeline.tail)).documentSource()
  }
}
