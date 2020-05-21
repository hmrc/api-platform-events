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
import uk.gov.hmrc.apiplatformevents.models.{ApiSubscribedEventModel,
  ApiUnsubscribedEventModel,
  ClientSecretAddedEventModel,
  ClientSecretRemovedEventModel,
  RedirectUrisUpdatedEventModel,
  TeamMemberAddedEventModel,
  TeamMemberRemovedEventModel}
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ApplicationEvent, EventType}

case class TeamMemberAddedEvent(override val applicationId: String,
                                override val eventDateTime: DateTime,
                                override val actor: Actor,
                                teamMemberEmail: String,
                                teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_ADDED
}

object TeamMemberAddedEvent {
  def fromRequest(event: TeamMemberAddedEventModel): ApplicationEvent = {
    TeamMemberAddedEvent(event.applicationId, event.eventDateTime, event.actor, event.teamMemberEmail, event.teamMemberRole)
  }
}

case class TeamMemberRemovedEvent(override val applicationId: String,
                                  override val eventDateTime: DateTime,
                                  override val actor: Actor,
                                  teamMemberEmail: String,
                                  teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_REMOVED
}

object TeamMemberRemovedEvent {
  def fromRequest(event: TeamMemberRemovedEventModel): ApplicationEvent = {
    TeamMemberRemovedEvent(event.applicationId, event.eventDateTime, event.actor, event.teamMemberEmail, event.teamMemberRole)
  }
}

case class ClientSecretAddedEvent(override val applicationId: String,
                                  override val eventDateTime: DateTime,
                                  override val actor: Actor,
                                  clientSecretId: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_ADDED
}

object ClientSecretAddedEvent {
  def fromRequest(event: ClientSecretAddedEventModel): ApplicationEvent = {
    ClientSecretAddedEvent(event.applicationId, event.eventDateTime, event.actor, event.clientSecretId)
  }
}

case class ClientSecretRemovedEvent(override val applicationId: String,
                                   override val eventDateTime: DateTime,
                                   override val actor: Actor,
                                   clientSecretId: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_REMOVED
}

object ClientSecretRemovedEvent {
  def fromRequest(event: ClientSecretRemovedEventModel): ApplicationEvent = {
    ClientSecretRemovedEvent(event.applicationId, event.eventDateTime, event.actor, event.clientSecretId)
  }
}

case class RedirectUrisUpdatedEvent(override val applicationId: String,
                                    override val eventDateTime: DateTime,
                                    override val actor: Actor,
                                    oldRedirectUris: String,
                                    newRedirectUris: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.REDIRECT_URIS_UPDATED
}

object RedirectUrisUpdatedEvent {
  def fromRequest(event: RedirectUrisUpdatedEventModel): ApplicationEvent = {
    RedirectUrisUpdatedEvent(event.applicationId, event.eventDateTime, event.actor, event.oldRedirectUris, event.newRedirectUris)
  }
}

case class ApiSubscribedEvent(override val applicationId: String,
                                    override val eventDateTime: DateTime,
                                    override val actor: Actor,
                                    context: String,
                                    version: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.API_SUBSCRIBED
}

object ApiSubscribedEvent {
  def fromRequest(event: ApiSubscribedEventModel): ApplicationEvent = {
    ApiSubscribedEvent(event.applicationId, event.eventDateTime, event.actor, event.context, event.version)
  }
}

case class ApiUnsubscribedEvent(override val applicationId: String,
                              override val eventDateTime: DateTime,
                              override val actor: Actor,
                              context: String,
                              version: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.API_UNSUBSCRIBED
}

object ApiUnsubscribedEvent {
  def fromRequest(event: ApiUnsubscribedEventModel): ApplicationEvent = {
    ApiUnsubscribedEvent(event.applicationId, event.eventDateTime, event.actor, event.context, event.version)
  }
}