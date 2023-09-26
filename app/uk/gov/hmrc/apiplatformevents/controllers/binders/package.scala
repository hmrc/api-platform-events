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

import java.{util => ju}
import scala.util.Try

import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.{EventTag, EventTags}

package object binders {

  private def applicationIdFromString(text: String): Either[String, ApplicationId] = {
    Try(ju.UUID.fromString(text)).toOption
      .toRight(s"Cannot accept $text as ApplicationId")
      .map(uuid => ApplicationId(uuid))
  }

  implicit def applicationIdPathBinder(implicit textBinder: PathBindable[String]): PathBindable[ApplicationId] = new PathBindable[ApplicationId] {

    override def bind(key: String, value: String): Either[String, ApplicationId] = {
      textBinder.bind(key, value).flatMap(applicationIdFromString)
    }

    override def unbind(key: String, applicationId: ApplicationId): String = {
      applicationId.value.toString()
    }
  }

  private def eventTagFromString(text: String): Either[String, EventTag] = {
    EventTags
      .fromString(text)
      .toRight(s"Cannot accept $text as EventTag")
  }

  implicit def eventTagQueryStringBindable(implicit textBinder: QueryStringBindable[String]) = new QueryStringBindable[EventTag] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, EventTag]] = {
      textBinder.bind(key, params).map(_.flatMap(eventTagFromString))
    }

    override def unbind(key: String, tag: EventTag): String = {
      textBinder.unbind(key, tag.toString())
    }
  }

}
