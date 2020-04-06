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

package uk.gov.hmrc.apiplatformevents.models.db

import org.joda.time.DateTime
import play.api.libs.json.Format
import play.api.libs.json.Json.format
import uk.gov.hmrc.apiplatformevents.models.common.Actor
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.apiplatformevents.models.EnumJson
import uk.gov.hmrc.apiplatformevents.models.JsonFormatters.actorFormat


object ApplicationEventType extends Enumeration{
  type AccessType = Value
  val TEAM_MEMBER_ADDED = Value

  implicit val applicationEventTypeFormat = EnumJson.enumFormat(ApplicationEventType)
}

case class ApiPlatformApplicationEvent(eventId: String,
                                       applicationId: String,
                                       actor: Actor,
                                       eventType: ApplicationEventType.Value,
                                       eventDateTime: DateTime,
                                       eventData: Map[String, String])

object ApiPlatformApplicationEvent extends ReactiveMongoFormats {

  implicit val formats: Format[ApiPlatformApplicationEvent] =
    format[ApiPlatformApplicationEvent]
}
