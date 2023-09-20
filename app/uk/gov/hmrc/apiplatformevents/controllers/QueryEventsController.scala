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

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventTag
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatformevents.models.DisplayEvent
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger

object QueryEventsController {
  case class QueryResponse(events: List[DisplayEvent])

  object QueryResponse {
    implicit val format = Json.format[QueryResponse]
  }
}

@Singleton
class QueryEventsController @Inject() (
    val env: Environment,
    service: ApplicationEventsService,
    playBodyParsers: PlayBodyParsers,
    cc: ControllerComponents
)(implicit
    val configuration: Configuration,
    ec: ExecutionContext
) extends BackendController(cc)
    with ApplicationLogger {

  import QueryEventsController._

  def query(applicationId: ApplicationId, eventTag: Option[EventTag], actorType: Option[String]) = Action.async { _ =>
    service
      .fetchEventsBy(applicationId, eventTag, actorType.flatMap(ActorType.apply))
      .map(seq =>
        if (seq.isEmpty) {
          NotFound("No application changes found")
        } else {
          val displayEvents = seq.sorted.map(DisplayEvent(_))
          Ok(Json.toJson(QueryResponse(displayEvents)))
        }
      )
  }

  def queryValues(applicationId: ApplicationId) = Action.async { _ =>
    service
      .fetchEventQueryValues(applicationId)
      .map[Result](_.fold(NotFound(""))(qv => Ok(Json.toJson(qv))))
  }
}
