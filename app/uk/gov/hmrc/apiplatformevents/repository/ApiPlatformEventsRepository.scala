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

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Format
import play.api.libs.json.Json.{format, toJsFieldJsValueWrapper}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}

case class ApiPlatformEventsDBModel(parameter1: String,
                                    parameter2: Option[String],
                                    telephoneNumber: Option[String],
                                    emailAddress: Option[String])

object ApiPlatformEventsDBModel extends ReactiveMongoFormats {
  implicit val formats: Format[ApiPlatformEventsDBModel] =
    format[ApiPlatformEventsDBModel]
}

@Singleton
class ApiPlatformEventsRepository @Inject()(
    mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[ApiPlatformEventsDBModel, BSONObjectID](
      "api-platform-events",
      mongoComponent.mongoConnector.db,
      ApiPlatformEventsDBModel.formats,
      ReactiveMongoFormats.objectIdFormats) {

  def createEntity(event: ApiPlatformEventsDBModel)(
      implicit ec: ExecutionContext): Future[Unit] =
    insert(event).map(_ => ())

}
