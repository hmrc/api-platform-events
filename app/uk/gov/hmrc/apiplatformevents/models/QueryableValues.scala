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

import play.api.libs.json._
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventTag
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.EventTagJsonFormatters

case class QueryableValues(eventTags: List[EventTag], actorTypes: List[ActorType])

object QueryableValues {
  import EventTagJsonFormatters._

  // Temporary replacement that will be redundant once here and GK use the common domain lib completely

  implicit val formatActorType = new Format[ActorType] {

    override def writes(o: ActorType): JsValue =
      Json.obj("description" -> o.displayText, "type" -> o.toString)

    override def reads(json: JsValue): JsResult[ActorType] = {
      (json match {
        case JsString(text) => ActorType.apply(text)
        case JsObject(obj)  =>
          obj
            .get("type")
            .flatMap(_ match {
              case JsString(t) => ActorType.apply(t)
              case _           => None
            })
        case _              => None
      }).fold[JsResult[ActorType]](JsError(s"Cannot find actor Type"))(JsSuccess(_))
    }
  }

  implicit val format = Json.format[QueryableValues]
}
