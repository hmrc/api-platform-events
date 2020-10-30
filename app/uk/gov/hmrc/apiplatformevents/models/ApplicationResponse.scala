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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Json, OFormat}

import scala.collection.immutable

case class ApplicationResponse(name: String, collaborators: Set[Collaborator]) {
  lazy val admins: Set[Collaborator] = collaborators.filter(_.role == Role.ADMINISTRATOR)
  lazy val adminEmails: Set[String] = admins.map(_.emailAddress)
}
object ApplicationResponse {
  implicit val applicationFmt: OFormat[ApplicationResponse] = Json.format[ApplicationResponse]
}

case class Collaborator(emailAddress: String, role: Role)
object Collaborator {
  implicit val collaboratorFmt: OFormat[Collaborator] = Json.format[Collaborator]
}

sealed trait Role extends EnumEntry
object Role extends  Enum[Role] with PlayJsonEnum[Role]  {
  val values: immutable.IndexedSeq[Role] = findValues

  case object  DEVELOPER extends Role
  case object  ADMINISTRATOR extends Role

}
