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

package uk.gov.hmrc.apiplatformevents.repository

import java.util.UUID

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import uk.gov.hmrc.apiplatformevents.models.ReactiveMongoFormatters
import uk.gov.hmrc.apiplatformevents.models.common.ApplicationEvent
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ApplicationEventsRepository @Inject()(mongoComponent: ReactiveMongoComponent)
                                           (implicit ec: ExecutionContext, val mat: Materializer)
    extends ReactiveRepository[ApplicationEvent, BSONObjectID](
      "application-events",
      mongoComponent.mongoConnector.db,
      ReactiveMongoFormatters.formatApplicationEvent,
      ReactiveMongoFormats.objectIdFormats) {

  def createEntity(event: ApplicationEvent): Future[Boolean] =
    insert(event).map(wr => wr.ok)

  def populateEventIds(): Future[Unit] = {
    val query : JsObject = Json.obj("id" -> Json.obj("$exists" -> false))

    def updateApplicationEvent(event: JsObject): Unit = {
      val mongoId = (event \ "_id").as[BSONObjectID]
      val queryForFindById = Json.obj("_id" -> mongoId)
      val setIdOnEvent = Json.obj("$set" -> Json.obj("id" -> UUID.randomUUID()))
      val f : Future[_] = findAndUpdate(query = queryForFindById , setIdOnEvent)
      f.onComplete {
        case Failure(e) => Logger.error("Failed to update event" + e.getMessage)
        case Success(_) => Logger.info(s"Success - Id added to event with _id: $mongoId")
      }
    }

    processAll(query, updateApplicationEvent)
  }

  private def processAll(query : JsObject,  function: JsObject => Unit): Future[Unit] = {
    import akka.stream.scaladsl.{Sink, Source}
    import reactivemongo.akkastream.{State, cursorProducer}

    val sourceOfEvents: Source[JsObject, Future[State]] =
      collection
        .find(query, Option.empty[JsObject])
        .cursor[JsObject]()
        .documentSource()

    sourceOfEvents.runWith(Sink.foreach(function)).map(_ => ())
  }
}
