/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.apiplatformevents.models.JsonRequestFormatters._
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

@Singleton
class ApplicationEventsController @Inject()(val env: Environment,
                                            service: ApplicationEventsService,
                                            playBodyParsers: PlayBodyParsers,
                                            cc: ControllerComponents)(
                                             implicit val configuration: Configuration,
                                             ec: ExecutionContext) extends BackendController(cc) {

 

def ppnsCallbackUriUpdated(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[PpnsCallBackUriUpdatedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def teamMemberAdded(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[TeamMemberAddedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def teamMemberRemoved(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[TeamMemberRemovedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def clientSecretAdded(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ClientSecretAddedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def clientSecretRemoved(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ClientSecretRemovedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def redirectUrisUpdated(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[RedirectUrisUpdatedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def apiSubscribed(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApiSubscribedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def apiUnsubscribed(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApiUnsubscribedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  override protected def withJsonBody[T]
  (f: T => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Future[Result] = {
    withJson(request.body)(f)
  }

  private def withJson[T](json: JsValue)(f: T => Future[Result])(implicit reads: Reads[T]): Future[Result] = {
    Try(json.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        Future.successful(UnprocessableEntity(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, JsError.toJson(errs))))
      case Failure(e) =>
        Future.successful(UnprocessableEntity(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, e.getMessage)))
    }
  }

  private def mapResult(result: Boolean): Status = {
    if (result) {
      Created
    } else {
      InternalServerError
    }
  }

  private def recovery: PartialFunction[Throwable, Result] = {
    case NonFatal(e) => Logger.info("An unexpected error occurred:", e)
      InternalServerError
  }
}