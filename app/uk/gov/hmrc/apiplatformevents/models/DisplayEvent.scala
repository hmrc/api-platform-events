/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.Instant

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{Actor, ApplicationId}
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.{ApplicationEvent, EventId, EventTags}

case class DisplayEvent(
    id: EventId,
    applicationId: ApplicationId,
    eventDateTime: Instant,
    actor: Actor,
    eventTagDescription: String,
    eventType: String,
    metaData: List[String]
)

object DisplayEvent {
  implicit val format: OFormat[DisplayEvent] = Json.format[DisplayEvent]

  def apply(evt: ApplicationEvent): DisplayEvent = {
    val (eventType, metaData) = ApplicationEvent.asMetaData(evt)

    DisplayEvent(evt.id, evt.applicationId, evt.eventDateTime, evt.actor, EventTags.tag(evt).description, eventType, metaData)
  }
}
