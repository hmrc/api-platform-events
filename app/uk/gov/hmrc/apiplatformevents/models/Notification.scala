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

import play.api.libs.json.Format
import uk.gov.hmrc.apiplatform.modules.common.domain.services.SimpleEnumJsonFormatting
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventId

enum NotificationStatus {
  case Sent, Failed
}

object NotificationStatus {

  def apply(text: String): Option[NotificationStatus] = NotificationStatus.values.find(_.toString().equalsIgnoreCase(text))

  def unsafeApply(text: String): NotificationStatus = apply(text).getOrElse(throw new RuntimeException(s"$text is not a valid Notification Status"))

  implicit val format: Format[NotificationStatus] =
    SimpleEnumJsonFormatting.createStringFormatFor[NotificationStatus]("Notification Status", apply, x => x.toString().toUpperCase())

}

case class Notification(eventId: EventId, lastUpdated: Instant, status: NotificationStatus)
