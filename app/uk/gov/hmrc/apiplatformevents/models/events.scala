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

import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ApplicationEvent, EventType}

case class TeamMemberAddedEvent(override val applicationId: String,
                                override val eventDateTime: DateTime,
                                override val actor: Actor,
                                teamMemberEmail: String,
                                teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_ADDED
}


case class TeamMemberRemovedEvent(override val applicationId: String,
                                  override val eventDateTime: DateTime,
                                  override val actor: Actor,
                                  teamMemberEmail: String,
                                  teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.TEAM_MEMBER_REMOVED
}

case class ClientSecretAddedEvent(override val applicationId: String,
                                  override val eventDateTime: DateTime,
                                  override val actor: Actor,
                                  clientSecretId: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_ADDED
}


case class ClientSecretRemovedEvent(override val applicationId: String,
                                    override val eventDateTime: DateTime,
                                    override val actor: Actor,
                                    clientSecretId: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.CLIENT_SECRET_REMOVED
}


case class PpnsCallBackUriUpdatedEvent(override val applicationId: String,
                                    override val eventDateTime: DateTime,
                                    override val actor: Actor,
                                    oldRedirectUris: String,
                                    newRedirectUris: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.PPNS_CALLBACK_URI_UPDATED
}

case class RedirectUrisUpdatedEvent(override val applicationId: String,
                                    override val eventDateTime: DateTime,
                                    override val actor: Actor,
                                    oldRedirectUris: String,
                                    newRedirectUris: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.REDIRECT_URIS_UPDATED
}



case class ApiSubscribedEvent(override val applicationId: String,
                              override val eventDateTime: DateTime,
                              override val actor: Actor,
                              context: String,
                              version: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.API_SUBSCRIBED
}


case class ApiUnsubscribedEvent(override val applicationId: String,
                                override val eventDateTime: DateTime,
                                override val actor: Actor,
                                context: String,
                                version: String) extends ApplicationEvent {
  override val eventType: EventType = EventType.API_UNSUBSCRIBED
}
