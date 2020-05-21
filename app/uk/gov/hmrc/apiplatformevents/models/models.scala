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

package uk.gov.hmrc.apiplatformevents.models

import org.joda.time.DateTime
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ApplicationEvent, EventType}

object ErrorCode extends Enumeration {
  type ErrorCode = Value

  val INVALID_REQUEST_PAYLOAD = Value("INVALID_REQUEST_PAYLOAD")
  val UNKNOWN_ERROR = Value("UNKNOWN_ERROR")
}

object JsErrorResponse {
  def apply(errorCode: ErrorCode.Value, message: JsValueWrapper): JsObject =
    Json.obj(
      "code" -> errorCode.toString,
      "message" -> message
    )
}

case class TeamMemberAddedEventModel(override val applicationId: String,
                                     override val eventDateTime: DateTime,
                                     override val actor: Actor,
                                     teamMemberEmail: String,
                                     teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_ADDED
}

case class TeamMemberRemovedEventModel(override val applicationId: String,
                                       override val eventDateTime: DateTime,
                                       override val actor: Actor,
                                       teamMemberEmail: String,
                                       teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_REMOVED
}

case class ClientSecretAddedEventModel(override val applicationId: String,
                                       override val eventDateTime: DateTime,
                                       override val actor: Actor,
                                       clientSecretId: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_ADDED
}

case class ClientSecretRemovedEventModel(override val applicationId: String,
                                         override val eventDateTime: DateTime,
                                         override val actor: Actor,
                                         clientSecretId: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_REMOVED
}

case class RedirectUrisUpdatedEventModel(override val applicationId: String,
                                         override val eventDateTime: DateTime,
                                         override val actor: Actor,
                                         oldRedirectUris: String,
                                         newRedirectUris: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.REDIRECT_URIS_UPDATED
}

case class ApiSubscribedEventModel(override val applicationId: String,
                                   override val eventDateTime: DateTime,
                                   override val actor: Actor,
                                   context: String,
                                   version: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.API_SUBSCRIBED
}

case class ApiUnsubscribedEventModel(override val applicationId: String,
                                     override val eventDateTime: DateTime,
                                     override val actor: Actor,
                                     context: String,
                                     version: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.API_UNSUBSCRIBED
}
