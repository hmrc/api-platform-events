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

package uk.gov.hmrc.apiplatformevents.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventTag
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.AbstractApplicationEvent
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId
import scala.concurrent.ExecutionContext

object QueryEventsController {
  case class QueryResponse(events: List[AbstractApplicationEvent])

  object QueryResponse {
    import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.EventsInterServiceCallJsonFormatters._
    implicit val format = Json.format[QueryResponse]
  }
}

@Singleton
class QueryEventsController @Inject()(
  val env: Environment,
  service: ApplicationEventsService,
  playBodyParsers: PlayBodyParsers,
  cc: ControllerComponents
)(
  implicit val configuration: Configuration,
  ec: ExecutionContext
) extends BackendController(cc) with ApplicationLogger {

  import QueryEventsController._

  def query(applicationId: ApplicationId, eventTag: Option[EventTag]) = Action.async { _ =>
    service.fetchEventsBy(applicationId, eventTag)
    .map( seq => 
      if(seq.isEmpty) 
        NotFound("No application changes found")
      else
        Ok(Json.toJson(QueryResponse(seq.sorted)))
    )
  }

  def queryValues(applicationId: ApplicationId) = Action.async { _ =>
    service.fetchEventQueryValues(applicationId)
    .map[Result](_.fold(NotFound(""))(qv => Ok(Json.toJson(qv))))
  }
}