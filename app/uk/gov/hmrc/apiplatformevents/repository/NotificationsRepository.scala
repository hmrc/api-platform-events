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

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import uk.gov.hmrc.apiplatformevents.models.Notification

object NotificationsRepository {
  import play.api.libs.json.{OFormat, Json}

  implicit val formatNotification: OFormat[Notification] = Json.format[Notification]
}

@Singleton
class NotificationsRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Notification](
      mongoComponent,
      "notifications",
      NotificationsRepository.formatNotification,
      indexes = Seq(
        IndexModel(
          ascending("eventId"),
          IndexOptions()
            .name("event_id_index")
            .unique(true)
            .background(true)
        )
      )
    ) {
  override lazy val requiresTtlIndex: Boolean = false

  def createEntity(notification: Notification): Future[Boolean] =
    collection.insertOne(notification).toFuture().map(wr => wr.wasAcknowledged())

}
