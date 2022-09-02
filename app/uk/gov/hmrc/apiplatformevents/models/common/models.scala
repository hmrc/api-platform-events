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

sealed trait ActorType extends EnumEntry

object ActorType extends Enum[ActorType] with PlayJsonEnum[ActorType] {
  val values: immutable.IndexedSeq[ActorType] = findValues

  case object COLLABORATOR extends ActorType
  case object GATEKEEPER extends  ActorType
  case object SCHEDULED_JOB extends ActorType
  case object UNKNOWN extends ActorType
}

case class OldActor(id: String, actorType: ActorType)

sealed trait Actor

object Actor {
  def extractActorText(actor: Actor): String = actor match {
    case GatekeeperUserActor(user) => user
    case CollaboratorActor(email) => email
  }
}

case class GatekeeperUserActor(user: String) extends Actor
case class CollaboratorActor(email: String) extends Actor
//case class ScheduledJobActor(jobId: String) extends Actor
//case class UnknownActor() extends Actor

sealed trait EventType extends EnumEntry

object EventType extends Enum[EventType] with PlayJsonEnum[EventType]  {
  val values: immutable.IndexedSeq[EventType] = findValues

  case object PROD_APP_NAME_CHANGED extends EventType
  case object PROD_APP_PRIVACY_POLICY_LOCATION_CHANGED extends EventType
  case object PROD_LEGACY_APP_PRIVACY_POLICY_LOCATION_CHANGED extends EventType
  case object PROD_APP_TERMS_CONDITIONS_LOCATION_CHANGED extends EventType
  case object PROD_LEGACY_APP_TERMS_CONDITIONS_LOCATION_CHANGED extends EventType
  case object RESPONSIBLE_INDIVIDUAL_SET extends EventType
  case object RESPONSIBLE_INDIVIDUAL_CHANGED extends EventType
  case object APPLICATION_STATE_CHANGED extends EventType
  case object RESPONSIBLE_INDIVIDUAL_VERIFICATION_STARTED extends EventType

  case object  API_SUBSCRIBED extends EventType
  case object  API_UNSUBSCRIBED extends EventType

  case object  CLIENT_SECRET_ADDED extends EventType
  case object  CLIENT_SECRET_REMOVED extends EventType

  case object  PPNS_CALLBACK_URI_UPDATED extends EventType

  case object  REDIRECT_URIS_UPDATED extends EventType

  case object  TEAM_MEMBER_ADDED extends EventType
  case object  TEAM_MEMBER_REMOVED extends EventType

}

case class EventId(value: UUID) extends AnyVal
object EventId {
  def random: EventId = EventId(UUID.randomUUID())
}

