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

package uk.gov.hmrc.apiplatformevents.repository

import java.util.UUID

import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.models.db.ApiPlatformApplicationEvent
import uk.gov.hmrc.apiplatformevents.models.db.ApplicationEventType._

object ApiPlatformApplicationEventAdaptor {

  def fromTeamMemberAddedEvent(event: TeamMemberAddedEvent): ApiPlatformApplicationEvent =
    ApiPlatformApplicationEvent(
      applicationId = event.applicationId,
      eventId = UUID.randomUUID().toString,
      actor = event.actor,
      eventType = TEAM_MEMBER_ADDED,
      eventDateTime = event.eventDateTime,
      eventData = Map("teamMemberEmail" -> event.teamMemberEmail, "teamMemberRole" -> event.teamMemberRole)
    )
}
