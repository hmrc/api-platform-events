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

package uk.gov.hmrc.apiplatformevents.controllers

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationEventsController @Inject()(val env: Environment,
                                            service: ApplicationEventsService,
                                            cc: ControllerComponents)(
                                           implicit val configuration: Configuration,
                                           ec: ExecutionContext) extends BackendController(cc) {

  def helloworld: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(
      Ok("hello world application")
    )

  }

  def teamMemberAdded: Action[AnyContent] = Action.async { implicit request =>
    implicit val formatter = TeamMemberAddedEvent.format
      request.body.asJson match {
        case Some(value) =>  handleEvent(value.as[TeamMemberAddedEvent])
        case None => Future.successful(BadRequest)
      }
  }

  def handleEvent(event: TeamMemberAddedEvent)
                 (implicit hc: HeaderCarrier): Future[Result] = {
    service.captureEvent(event) map {
      case true => Created
      case false =>  InternalServerError
    }
  }




}