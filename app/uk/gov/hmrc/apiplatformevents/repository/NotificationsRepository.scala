/*
 * Copyright 2021 HM Revenue & Customs
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
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.apiplatformevents.models.{Notification, ReactiveMongoFormatters}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationsRepository @Inject()(mongoComponent: ReactiveMongoComponent)
                                       (implicit ec: ExecutionContext)
  extends ReactiveRepository[Notification, BSONObjectID](
      "notifications",
      mongoComponent.mongoConnector.db,
      ReactiveMongoFormatters.formatNotification,
      ReactiveMongoFormats.objectIdFormats) {

  override def indexes = Seq(
    Index(
      key = Seq("eventId" -> IndexType.Ascending),
      name = Some("event_id_index"),
      unique = true,
      background = true
    )
  )

  def createEntity(notification: Notification): Future[Boolean] =
    insert(notification).map(wr => wr.ok)

}
