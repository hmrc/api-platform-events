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

package uk.gov.hmrc.apiplatformevents.models.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import org.joda.time.DateTime

import scala.collection.immutable


sealed trait ActorType extends EnumEntry

object ActorType extends Enum[ActorType] with PlayJsonEnum[ActorType] {
  val values: immutable.IndexedSeq[ActorType] = findValues

  case object COLLABORATOR extends ActorType
  case object GATEKEEPER extends  ActorType
  case object SCHEDULED_JOB extends ActorType
}

case class Actor(id: String, actorType: ActorType)

sealed trait EventType extends EnumEntry

object EventType extends  Enum[EventType] with PlayJsonEnum[EventType]  {
  val values: immutable.IndexedSeq[EventType] = findValues

  case object TEAM_MEMBER_ADDED extends EventType
  case object  TEAM_MEMBER_REMOVED extends EventType
  case object  CLIENT_SECRET_ADDED extends EventType
  case object  CLIENT_SECRET_REMOVED extends EventType
  case object  REDIRECT_URIS_UPDATED extends EventType
  case object  API_SUBSCRIBED extends EventType
  case object  API_UNSUBSCRIBED extends EventType
}

trait ApplicationEvent{
  val applicationId: String
  val eventDateTime: DateTime
  val eventType: EventType
  val actor: Actor
}
