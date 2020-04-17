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
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType}
import uk.gov.hmrc.play.json.Union


import scala.language.implicitConversions

object JodaDateFormats {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val JodaDateReads: Reads[org.joda.time.DateTime] = JodaReads.jodaDateReads(dateFormat)
  implicit val JodaDateWrites: Writes[org.joda.time.DateTime] = JodaWrites.jodaDateWrites(dateFormat)
  implicit val JodaDateTimeFormat: Format[DateTime] = Format(JodaDateReads, JodaDateWrites)
}

object JsonFormatters {
  implicit val dateReads = JodaDateFormats.JodaDateTimeFormat
  implicit val actorFormat = Json.format[Actor]
  implicit val teamMemberAddedEventFormats = Json.format[TeamMemberAddedEvent]
  implicit val teamMemberRemovedEventFormats = Json.format[TeamMemberRemovedEvent]
  implicit val clientSecretAddedEventFormats = Json.format[ClientSecretAddedEvent]
  implicit val clientSecretRemovedEventFormats = Json.format[ClientSecretRemovedEvent]
  implicit val urisUpdatedEventFormats = Json.format[RedirectUrisUpdatedEvent]
  implicit val formatApplicationEvent: Format[ApplicationEvent] = Union.from[ApplicationEvent]("eventType")
    .and[TeamMemberAddedEvent](EventType.TEAM_MEMBER_ADDED.toString)
    .and[TeamMemberRemovedEvent](EventType.TEAM_MEMBER_REMOVED.toString)
    .and[ClientSecretAddedEvent](EventType.CLIENT_SECRET_ADDED.toString)
    .and[ClientSecretRemovedEvent](EventType.CLIENT_SECRET_REMOVED.toString)
    .and[RedirectUrisUpdatedEvent](EventType.REDIRECT_URIS_UPDATED.toString)
    .format
}

class InvalidEnumException(className: String, input:String)
  extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException => throw new InvalidEnumException(enum.getClass.getSimpleName, s)
        }
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }



}
