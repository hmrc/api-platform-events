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

package uk.gov.hmrc.apiplatformevents.data

import uk.gov.hmrc.apiplatformevents.models.{ApiSubscribedEvent, ApiUnsubscribedEvent, ClientSecretAddedEvent, ClientSecretRemovedEvent, PpnsCallBackUriUpdatedEvent, ProductionAppNameChangedEvent, RedirectUrisUpdatedEvent, TeamMemberAddedEvent, TeamMemberRemovedEvent}
import uk.gov.hmrc.apiplatformevents.models.common.{OldActor, OldActorType, EventId}

import java.time.LocalDateTime
import java.util.UUID

trait ApplicationEventTestData {

  val teamMemberAddedModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = EventId(UUID.fromString("21dbf54a-aa62-4217-a5c6-17e085e42105")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    teamMemberEmail = "jkhkhk",
    teamMemberRole = "ADMIN")

  val teamMemberRemovedModel: TeamMemberRemovedEvent = TeamMemberRemovedEvent(
    id = EventId(UUID.fromString("45a39393-d3a8-4c1b-9817-f4b8828b1b65")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    teamMemberEmail = "jkhkhk",
    teamMemberRole = "ADMIN")

  val clientSecretAddedModel: ClientSecretAddedEvent = ClientSecretAddedEvent(
    id = EventId(UUID.fromString("7a152fa7-45e2-4fcf-9ef3-0b62b49bdc23")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    clientSecretId = "jkhkhk")

  val clientSecretRemovedModel: ClientSecretRemovedEvent = ClientSecretRemovedEvent(
    id = EventId(UUID.fromString("3afd75ae-6a42-425a-b650-7259c3ba72fa")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    clientSecretId = "jkhkhk")

  val redirectUrisUpdatedModel: RedirectUrisUpdatedEvent = RedirectUrisUpdatedEvent(
    id = EventId(UUID.fromString("04c391dc-2a16-4e6a-b5c8-aa10db742e3f")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    oldRedirectUris = "oldru",
    newRedirectUris = "newru")

  val apiSubscribedModel: ApiSubscribedEvent = ApiSubscribedEvent(
    id = EventId(UUID.fromString("5308887f-f6d8-49f5-b2b4-8280fa7a5e60")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    context = "apicontext",
    version = "1.0")

  val apiUnsubscribedModel: ApiUnsubscribedEvent = ApiUnsubscribedEvent(
    id = EventId(UUID.fromString("7005e378-1ed3-46d9-b786-fc0202807cf6")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d4",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    context = "apicontext",
    version = "1.0")

  val ppnsCallBackUriUpdatedEvent: PpnsCallBackUriUpdatedEvent = PpnsCallBackUriUpdatedEvent(
    id = EventId(UUID.fromString("a5baebbb-a69d-4434-ba7a-573c274ffd03")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d41",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    boxId = "boxId",
    boxName = "boxName",
    oldCallbackUrl = "some/url/",
    newCallbackUrl = "some/url/here")

  val productionAppNameChangedEvent: ProductionAppNameChangedEvent = ProductionAppNameChangedEvent(
    id = EventId(UUID.fromString("a5baebbb-a69d-4434-ba7a-573c274ffd03")),
    applicationId = "e174ec96-5bd9-4530-91d8-473f019e5d41",
    eventDateTime = LocalDateTime.now(),
    OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    oldAppName = "old app name",
    newAppName = "new app name",
    requestingAdminName = "mr admin")
}
