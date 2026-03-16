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

package uk.gov.hmrc.apiplatformevents.controllers.ppns

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

import play.api.libs.json.JsValue
import play.api.mvc.*
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.*
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.EventsInterServiceCallJsonFormatters

import uk.gov.hmrc.apiplatformevents.controllers.AbstractEventsController
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService

@Singleton
class PpnsEventsController @Inject() (
    val env: Environment,
    service: ApplicationEventsService,
    playBodyParsers: PlayBodyParsers,
    cc: ControllerComponents
)(implicit
    val configuration: Configuration,
    ec: ExecutionContext
) extends AbstractEventsController(cc) {

  import EventsInterServiceCallJsonFormatters.given

  def ppnsCallbackUriUpdated(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApplicationEvents.PpnsCallBackUriUpdatedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }
}
