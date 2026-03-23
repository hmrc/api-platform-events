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

package uk.gov.hmrc.apiplatformevents.controllers

import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventTag

object Binders {
  import uk.gov.hmrc.apiplatform.modules.common.domain.services.EnumJsonHelper.*

  private def eventTagFromString(text: String): Either[String, EventTag] = {
    EventTag.apply(fromScreamingSnakeCase(text)).toRight(s"Cannot accept $text as EventTag")
  }

  implicit def eventTagQueryStringBindable(implicit textBinder: QueryStringBindable[String]): QueryStringBindable[EventTag] = new QueryStringBindable[EventTag] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, EventTag]] = {
      textBinder.bind(key, params).map(_.flatMap(txt => eventTagFromString(txt)))
    }

    override def unbind(key: String, tag: EventTag): String = {
      textBinder.unbind(key, tag.toString())
    }
  }

}
