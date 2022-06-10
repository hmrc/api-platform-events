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
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ApplicationEvent, EventId, EventType}
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.play.json.Union
import org.bson.codecs.Codec
import scala.reflect.runtime.universe._


object MongoFormatters {

    def allCodecs[P](
      format: Format[P],
      legacyNumbers: Boolean = false
    )(implicit tt: TypeTag[P]): Seq[Codec[_]] = {

      val clazz =  tt.tpe.typeSymbol.asClass
      require(clazz.isSealed && clazz.isTrait)
      
      val subs = clazz.knownDirectSubclasses

      subs.toSeq.map { s =>
        UnionCodecs.playUnionFormatCodec(format, legacyNumbers)
      }
    }


  trait UnionCodecs extends Codecs {
    def playUnionFormatCodec[S <: P, P](
      format: Format[P],
      legacyNumbers: Boolean = false
    )(implicit tag: TypeTag[S]): Codec[S] = new Codec[S] {
    
      override def getEncoderClass: Class[S] = {
        val mirror = tag.mirror
        val clazz = mirror.runtimeClass(tag.tpe.typeSymbol.asClass)   
        clazz.asInstanceOf[Class[S]]
      }
    }
  }


  object UnionCodecs extends UnionCodecs


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

  val mongoCodes = allCodecs[ApplicationEvent](formatApplicationEvent)
  // val mongoCodecs = Seq(
    // Codecs.playFormatCodec(formatApplicationEvent),
    // Codecs.playFormatCodec(EventType.jsonFormat),
    // Codecs.playFormatCodec(eventIdFormat),
    // Codecs.playFormatCodec(actorFormat),
    // // Codecs.playFormatCodec[TeamMemberAddedEvent](formatApplicationEvent),
    // // Codecs.playFormatCodec[TeamMemberRemovedEvent](formatApplicationEvent),
    // Codecs.playFormatCodec(clientSecretAddedEventFormats),
    // Codecs.playFormatCodec(clientSecretRemovedEventFormats),
    // Codecs.playFormatCodec(urisUpdatedEventFormats),
    // Codecs.playFormatCodec(apiSubscribedEventFormats),
    // Codecs.playFormatCodec(apiUnsubscribedEventFormats),
    // Codecs.playFormatCodec(apiUnsubscribedEventFormats),
    // Codecs.playFormatCodec(PpnsCallBackUriUpdatedEventFormats),
    // new ObjectIdCodec
  // )

  implicit val formatNotification: OFormat[Notification] = Json.format[Notification]
}

object JsonRequestFormatters {

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
