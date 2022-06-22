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

import uk.gov.hmrc.apiplatformevents.models.common.{Actor, EventId, OldActor}

import java.time.LocalDateTime

sealed trait ApplicationEvent {
  def id: EventId
  def applicationId: String
  def eventDateTime: LocalDateTime
}

case class TeamMemberAddedEvent(id: EventId,
                                applicationId: String,
                                eventDateTime: LocalDateTime,
                                actor: OldActor,
                                teamMemberEmail: String,
                                teamMemberRole: String) extends ApplicationEvent {
}

case class TeamMemberRemovedEvent(id: EventId,
                                  applicationId: String,
                                  eventDateTime: LocalDateTime,
                                  actor: OldActor,
                                  teamMemberEmail: String,
                                  teamMemberRole: String) extends ApplicationEvent {
}

case class ClientSecretAddedEvent(id: EventId,
                                  applicationId: String,
                                  eventDateTime: LocalDateTime,
                                  actor: OldActor,
                                  clientSecretId: String) extends ApplicationEvent {
}

case class ClientSecretRemovedEvent(id: EventId,
                                    applicationId: String,
                                    eventDateTime: LocalDateTime,
                                    actor: OldActor,
                                    clientSecretId: String) extends ApplicationEvent {
}


case class PpnsCallBackUriUpdatedEvent(id: EventId,
                                       applicationId: String,
                                       eventDateTime: LocalDateTime,
                                       actor: OldActor,
                                       boxId: String,
                                       boxName: String,
                                       oldCallbackUrl: String,
                                       newCallbackUrl: String) extends ApplicationEvent {
}

case class RedirectUrisUpdatedEvent(id: EventId,
                                    applicationId: String,
                                    eventDateTime: LocalDateTime,
                                    actor: OldActor,
                                    oldRedirectUris: String,
                                    newRedirectUris: String) extends ApplicationEvent {
}

case class ApiSubscribedEvent(id: EventId,
                              applicationId: String,
                              eventDateTime: LocalDateTime,
                              actor: OldActor,
                              context: String,
                              version: String) extends ApplicationEvent {
}

case class ApiUnsubscribedEvent(id: EventId,
                                applicationId: String,
                                eventDateTime: LocalDateTime,
                                actor: OldActor,
                                context: String,
                                version: String) extends ApplicationEvent {
}

case class ProductionAppNameChangedEvent(id: EventId,
                                         applicationId: String,
                                         eventDateTime: LocalDateTime,
                                         actor: Actor,
                                         oldAppName: String,
                                         newAppName: String,
                                         requestingAdminName: String) extends ApplicationEvent 