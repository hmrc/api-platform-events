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
import org.joda.time.DateTime
import play.api.libs.json.Format
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.apiplatformevents.models.ReactiveMongoFormattersV1
import uk.gov.hmrc.apiplatformevents.models.common.ApplicationEvent
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationEventsV1Repository @Inject()(
    mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[ApplicationEvent, BSONObjectID](
      "api-platform-application-events",
      mongoComponent.mongoConnector.db,
      ReactiveMongoFormattersV1.formatApplicationEvent,
      ReactiveMongoFormats.objectIdFormats) {

  def createEntity(event: ApplicationEvent)
                  (implicit ec: ExecutionContext): Future[Boolean] =
    insert(event).map(wr => wr.ok)

}
