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


import play.api.libs.json._
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType, EventId, EventType, GatekeeperUserActor, OldActor, OldEventType}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.play.json.Union

object MongoFormatters extends MongoJavatimeFormats.Implicits {

  implicit val eventIdFormat: Format[EventId] = Json.valueFormat[EventId]
  implicit val oldActorFormat: OFormat[OldActor] = Json.format[OldActor]
  implicit val gatekeeperUserActorFormat: OFormat[GatekeeperUserActor] = Json.format[GatekeeperUserActor]
  implicit val formatActor: OFormat[Actor] = Union.from[Actor]("actorType")
    .and[GatekeeperUserActor](ActorType.GATEKEEPER.toString)
    .format

  implicit val teamMemberAddedEventFormats: OFormat[TeamMemberAddedEvent] = Json.format[TeamMemberAddedEvent]
  implicit val teamMemberRemovedEventFormats: OFormat[TeamMemberRemovedEvent] = Json.format[TeamMemberRemovedEvent]
  implicit val clientSecretAddedEventFormats: OFormat[ClientSecretAddedEvent] = Json.format[ClientSecretAddedEvent]
  implicit val clientSecretRemovedEventFormats: OFormat[ClientSecretRemovedEvent] = Json.format[ClientSecretRemovedEvent]
  implicit val urisUpdatedEventFormats: OFormat[RedirectUrisUpdatedEvent] = Json.format[RedirectUrisUpdatedEvent]
  implicit val apiSubscribedEventFormats: OFormat[ApiSubscribedEvent] = Json.format[ApiSubscribedEvent]
  implicit val apiUnsubscribedEventFormats: OFormat[ApiUnsubscribedEvent] = Json.format[ApiUnsubscribedEvent]
  implicit val productionAppNameChangedEventFormats: OFormat[ProductionAppNameChangedEvent] = Json.format[ProductionAppNameChangedEvent]
  implicit val PpnsCallBackUriUpdatedEventFormats: OFormat[PpnsCallBackUriUpdatedEvent] = Json.format[PpnsCallBackUriUpdatedEvent]
  
  implicit val formatOldApplicationEvent: OFormat[OldApplicationEvent] = Union.from[OldApplicationEvent]("eventType")
    .and[TeamMemberRemovedEvent](OldEventType.TEAM_MEMBER_REMOVED.toString)
    .and[TeamMemberAddedEvent](OldEventType.TEAM_MEMBER_ADDED.toString)
    .and[ClientSecretAddedEvent](OldEventType.CLIENT_SECRET_ADDED.toString)
    .and[ClientSecretRemovedEvent](OldEventType.CLIENT_SECRET_REMOVED.toString)
    .and[PpnsCallBackUriUpdatedEvent](OldEventType.PPNS_CALLBACK_URI_UPDATED.toString())
    .and[RedirectUrisUpdatedEvent](OldEventType.REDIRECT_URIS_UPDATED.toString)
    .and[ApiSubscribedEvent](OldEventType.API_SUBSCRIBED.toString)
    .and[ApiUnsubscribedEvent](OldEventType.API_UNSUBSCRIBED.toString)
    .format

  implicit val formatApplicationEvent: OFormat[ApplicationEvent] = Union.from[ApplicationEvent]("eventType")
    .and[ProductionAppNameChangedEvent](EventType.PROD_APP_NAME_CHANGED.toString)
    .format

  val mongoCodecsForOldAppEvents = Codecs.unionCodecs[OldApplicationEvent](formatOldApplicationEvent)
  val mongoCodecsForAppEvents = Codecs.unionCodecs[ApplicationEvent](formatApplicationEvent)

  implicit val formatNotification: OFormat[Notification] = Json.format[Notification]
}

object JsonRequestFormatters {

  implicit val eventIdFormat: Format[EventId] = Json.valueFormat[EventId]
  implicit val oldActorFormat: OFormat[OldActor] = Json.format[OldActor]
  implicit val gatekeeperUserActorFormat: OFormat[GatekeeperUserActor] = Json.format[GatekeeperUserActor]
  implicit val formatActor: OFormat[Actor] = Union.from[Actor]("actorType")
    .and[GatekeeperUserActor](ActorType.GATEKEEPER.toString)
    .format

  implicit val teamMemberAddedEventFormats: OFormat[TeamMemberAddedEvent] = Json.format[TeamMemberAddedEvent]
  implicit val teamMemberRemovedEventFormats: OFormat[TeamMemberRemovedEvent] = Json.format[TeamMemberRemovedEvent]
  implicit val clientSecretAddedEventFormats: OFormat[ClientSecretAddedEvent] = Json.format[ClientSecretAddedEvent]
  implicit val clientSecretRemovedEventFormats: OFormat[ClientSecretRemovedEvent] = Json.format[ClientSecretRemovedEvent]
  implicit val urisUpdatedEventFormats: OFormat[RedirectUrisUpdatedEvent] = Json.format[RedirectUrisUpdatedEvent]
  implicit val apiSubscribedEventFormats: OFormat[ApiSubscribedEvent] = Json.format[ApiSubscribedEvent]
  implicit val apiUnsubscribedEventFormats: OFormat[ApiUnsubscribedEvent] = Json.format[ApiUnsubscribedEvent]
  implicit val productionAppNameChangedEventFormats: OFormat[ProductionAppNameChangedEvent] = Json.format[ProductionAppNameChangedEvent]
  implicit val PpnsCallBackUriUpdatedEventFormats: OFormat[PpnsCallBackUriUpdatedEvent] = Json.format[PpnsCallBackUriUpdatedEvent]
  implicit val formatOldApplicationEvent: Format[OldApplicationEvent] = Union.from[OldApplicationEvent]("eventType")
    .and[TeamMemberAddedEvent](OldEventType.TEAM_MEMBER_ADDED.toString)
    .and[TeamMemberRemovedEvent](OldEventType.TEAM_MEMBER_REMOVED.toString)
    .and[ClientSecretAddedEvent](OldEventType.CLIENT_SECRET_ADDED.toString)
    .and[ClientSecretRemovedEvent](OldEventType.CLIENT_SECRET_REMOVED.toString)
    .and[RedirectUrisUpdatedEvent](OldEventType.REDIRECT_URIS_UPDATED.toString)
    .and[PpnsCallBackUriUpdatedEvent](OldEventType.PPNS_CALLBACK_URI_UPDATED.toString)
    .and[ApiSubscribedEvent](OldEventType.API_SUBSCRIBED.toString)
    .and[ApiUnsubscribedEvent](OldEventType.API_UNSUBSCRIBED.toString)
    .format

  implicit val formatApplicationEvent: Format[ApplicationEvent] = Union.from[ApplicationEvent]("eventType")
    .and[ProductionAppNameChangedEvent](EventType.PROD_APP_NAME_CHANGED.toString)
    .format
}
