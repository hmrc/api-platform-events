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

package uk.gov.hmrc.apiplatformevents.models.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import java.util.UUID
import scala.collection.immutable


sealed trait OldActorType extends EnumEntry

object OldActorType extends Enum[OldActorType] with PlayJsonEnum[OldActorType] {
  val values: immutable.IndexedSeq[OldActorType] = findValues

  case object COLLABORATOR extends OldActorType
  case object GATEKEEPER extends  OldActorType
  case object SCHEDULED_JOB extends OldActorType
  case object UNKNOWN extends OldActorType
}

case class OldActor(id: String, actorType: OldActorType)

sealed trait OldEventType extends EnumEntry

object OldEventType extends  Enum[OldEventType] with PlayJsonEnum[OldEventType]  {
  val values: immutable.IndexedSeq[OldEventType] = findValues

  case object  API_SUBSCRIBED extends OldEventType
  case object  API_UNSUBSCRIBED extends OldEventType

  case object  CLIENT_SECRET_ADDED extends OldEventType
  case object  CLIENT_SECRET_REMOVED extends OldEventType

  case object  PPNS_CALLBACK_URI_UPDATED extends OldEventType

  case object  REDIRECT_URIS_UPDATED extends OldEventType

  case object  TEAM_MEMBER_ADDED extends OldEventType
  case object  TEAM_MEMBER_REMOVED extends OldEventType

  case object  PROD_APP_NAME_CHANGED extends OldEventType
}

case class EventId(value: UUID) extends AnyVal
object EventId {
  def random: EventId = EventId(UUID.randomUUID())
}

