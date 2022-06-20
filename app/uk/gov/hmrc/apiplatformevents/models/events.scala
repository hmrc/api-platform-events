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

package uk.gov.hmrc.apiplatformevents.models

import uk.gov.hmrc.apiplatformevents.models.common.{OldActor, EventId, EventType}

import java.time.LocalDateTime

sealed trait OldApplicationEvent {
  val id: EventId
  val applicationId: String
  val eventDateTime: LocalDateTime
  val actor: OldActor

  def eventType: EventType
}

case class TeamMemberAddedEvent(override val id: EventId,
                                override val applicationId: String,
                                override val eventDateTime: LocalDateTime,
                                override val actor: OldActor,
                                teamMemberEmail: String,
                                teamMemberRole: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_ADDED
}

case class TeamMemberRemovedEvent(override val id: EventId,
                                  override val applicationId: String,
                                  override val eventDateTime: LocalDateTime,
                                  override val actor: OldActor,
                                  teamMemberEmail: String,
                                  teamMemberRole: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_REMOVED
}

case class ClientSecretAddedEvent(override val id: EventId,
                                  override val applicationId: String,
                                  override val eventDateTime: LocalDateTime,
                                  override val actor: OldActor,
                                  clientSecretId: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_ADDED
}

case class ClientSecretRemovedEvent(override val id: EventId,
                                    override val applicationId: String,
                                    override val eventDateTime: LocalDateTime,
                                    override val actor: OldActor,
                                    clientSecretId: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_REMOVED
}


case class PpnsCallBackUriUpdatedEvent(override val id: EventId,
                                       override val applicationId: String,
                                       override val eventDateTime: LocalDateTime,
                                       override val actor: OldActor,
                                       boxId: String,
                                       boxName: String,
                                       oldCallbackUrl: String,
                                       newCallbackUrl: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.PPNS_CALLBACK_URI_UPDATED
}

case class RedirectUrisUpdatedEvent(override val id: EventId,
                                    override val applicationId: String,
                                    override val eventDateTime: LocalDateTime,
                                    override val actor: OldActor,
                                    oldRedirectUris: String,
                                    newRedirectUris: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.REDIRECT_URIS_UPDATED
}

case class ApiSubscribedEvent(override val id: EventId,
                              override val applicationId: String,
                              override val eventDateTime: LocalDateTime,
                              override val actor: OldActor,
                              context: String,
                              version: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.API_SUBSCRIBED
}

case class ApiUnsubscribedEvent(override val id: EventId,
                                override val applicationId: String,
                                override val eventDateTime: LocalDateTime,
                                override val actor: OldActor,
                                context: String,
                                version: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.API_UNSUBSCRIBED
}

case class ProductionAppNameChangedEvent(override val id: EventId,
                                         override val applicationId: String,
                                         override val eventDateTime: LocalDateTime,
                                         override val actor: OldActor,
                                         oldAppName: String,
                                         newAppName: String,
                                         requestingAdminName: String) extends OldApplicationEvent {
  override val eventType: EventType = EventType.PROD_APP_NAME_CHANGED
}
