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
import play.api.libs.json._
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ApplicationEvent, EventId, EventType}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.play.json.Union

import scala.language.implicitConversions

object JodaDateFormats {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val JodaDateReads: Reads[org.joda.time.DateTime] = JodaReads.jodaDateReads(dateFormat)
  implicit val JodaDateWrites: Writes[org.joda.time.DateTime] = JodaWrites.jodaDateWrites(dateFormat)
  implicit val JodaDateTimeFormat: Format[DateTime] = Format(JodaDateReads, JodaDateWrites)
}

object ReactiveMongoFormatters {
  implicit val dateReads: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
  implicit val eventIdFormat: Format[EventId] = Json.valueFormat[EventId]
  implicit val actorFormat: OFormat[Actor] = Json.format[Actor]
  implicit val teamMemberAddedEventFormats: OFormat[TeamMemberAddedEvent] = Json.format[TeamMemberAddedEvent]
  implicit val teamMemberRemovedEventFormats: OFormat[TeamMemberRemovedEvent] = Json.format[TeamMemberRemovedEvent]
  implicit val clientSecretAddedEventFormats: OFormat[ClientSecretAddedEvent] = Json.format[ClientSecretAddedEvent]
  implicit val clientSecretRemovedEventFormats: OFormat[ClientSecretRemovedEvent] = Json.format[ClientSecretRemovedEvent]
  implicit val urisUpdatedEventFormats: OFormat[RedirectUrisUpdatedEvent] = Json.format[RedirectUrisUpdatedEvent]
  implicit val apiSubscribedEventFormats: OFormat[ApiSubscribedEvent] = Json.format[ApiSubscribedEvent]
  implicit val apiUnsubscribedEventFormats: OFormat[ApiUnsubscribedEvent] = Json.format[ApiUnsubscribedEvent]
  implicit val PpnsCallBackUriUpdatedEventFormats: OFormat[PpnsCallBackUriUpdatedEvent] = Json.format[PpnsCallBackUriUpdatedEvent]
  implicit val formatApplicationEvent: Format[ApplicationEvent] = Union.from[ApplicationEvent]("eventType")
    .and[TeamMemberAddedEvent](EventType.TEAM_MEMBER_ADDED.toString)
    .and[TeamMemberRemovedEvent](EventType.TEAM_MEMBER_REMOVED.toString)
    .and[ClientSecretAddedEvent](EventType.CLIENT_SECRET_ADDED.toString)
    .and[ClientSecretRemovedEvent](EventType.CLIENT_SECRET_REMOVED.toString)
    .and[PpnsCallBackUriUpdatedEvent](EventType.PPNS_CALLBACK_URI_UPDATED.toString())
    .and[RedirectUrisUpdatedEvent](EventType.REDIRECT_URIS_UPDATED.toString)
    .and[ApiSubscribedEvent](EventType.API_SUBSCRIBED.toString)
    .and[ApiUnsubscribedEvent](EventType.API_UNSUBSCRIBED.toString)
    .format
  implicit val formatNotification: OFormat[Notification] = Json.format[Notification]
}

object JsonRequestFormatters {
  implicit val dateReads: Format[DateTime] = JodaDateFormats.JodaDateTimeFormat
  implicit val eventIdFormat: Format[EventId] = Json.valueFormat[EventId]
  implicit val actorFormat: OFormat[Actor] = Json.format[Actor]
  implicit val teamMemberAddedEventFormats: OFormat[TeamMemberAddedEvent] = Json.format[TeamMemberAddedEvent]
  implicit val teamMemberRemovedEventFormats: OFormat[TeamMemberRemovedEvent] = Json.format[TeamMemberRemovedEvent]
  implicit val clientSecretAddedEventFormats: OFormat[ClientSecretAddedEvent] = Json.format[ClientSecretAddedEvent]
  implicit val clientSecretRemovedEventFormats: OFormat[ClientSecretRemovedEvent] = Json.format[ClientSecretRemovedEvent]
  implicit val urisUpdatedEventFormats: OFormat[RedirectUrisUpdatedEvent] = Json.format[RedirectUrisUpdatedEvent]
  implicit val apiSubscribedEventFormats: OFormat[ApiSubscribedEvent] = Json.format[ApiSubscribedEvent]
  implicit val apiUnsubscribedEventFormats: OFormat[ApiUnsubscribedEvent] = Json.format[ApiUnsubscribedEvent]
  implicit val PpnsCallBackUriUpdatedEventFormats: OFormat[PpnsCallBackUriUpdatedEvent] = Json.format[PpnsCallBackUriUpdatedEvent]
  implicit val formatApplicationEvent: Format[ApplicationEvent] = Union.from[ApplicationEvent]("eventType")
    .and[TeamMemberAddedEvent](EventType.TEAM_MEMBER_ADDED.toString)
    .and[TeamMemberRemovedEvent](EventType.TEAM_MEMBER_REMOVED.toString)
    .and[ClientSecretAddedEvent](EventType.CLIENT_SECRET_ADDED.toString)
    .and[ClientSecretRemovedEvent](EventType.CLIENT_SECRET_REMOVED.toString)
    .and[RedirectUrisUpdatedEvent](EventType.REDIRECT_URIS_UPDATED.toString)
    .and[PpnsCallBackUriUpdatedEvent](EventType.PPNS_CALLBACK_URI_UPDATED.toString())
    .and[ApiSubscribedEvent](EventType.API_SUBSCRIBED.toString)
    .and[ApiUnsubscribedEvent](EventType.API_UNSUBSCRIBED.toString)
    .format
}
