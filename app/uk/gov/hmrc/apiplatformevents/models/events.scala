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
import uk.gov.hmrc.apiplatformevents.models.common.Actor


object EventType extends Enumeration{
  type AccessType = Value
  val TEAM_MEMBER_ADDED = Value

  implicit val applicationEventTypeFormat = EnumJson.enumFormat(EventType)
}

trait ApplicationEvent{
  val applicationId: String
  val eventDateTime: DateTime
  val eventType: EventType.Value
  val actor: Actor
}

case class TeamMemberAddedEvent(override val applicationId: String,
                                override val eventDateTime: DateTime,
                                override val actor: Actor,
                                teamMemberEmail: String,
                                teamMemberRole: String) extends ApplicationEvent {
  override val eventType: EventType.Value = EventType.TEAM_MEMBER_ADDED
}