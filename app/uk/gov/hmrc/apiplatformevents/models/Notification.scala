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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import java.time.LocalDateTime

import uk.gov.hmrc.apiplatformevents.models.common.EventId

import scala.collection.immutable

case class Notification(eventId: EventId,
                        lastUpdated: LocalDateTime,
                        status: NotificationStatus)

sealed trait NotificationStatus extends EnumEntry

object NotificationStatus extends  Enum[NotificationStatus] with PlayJsonEnum[NotificationStatus]  {
  val values: immutable.IndexedSeq[NotificationStatus] = findValues

  case object SENT extends NotificationStatus
  case object FAILED extends NotificationStatus
}
