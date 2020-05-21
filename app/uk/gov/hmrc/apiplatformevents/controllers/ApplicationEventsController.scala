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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.apiplatformevents.models.JsonRequestFormatters._
import uk.gov.hmrc.apiplatformevents.models.{ApiSubscribedEventModel,
  ApiUnsubscribedEventModel,
  ClientSecretAddedEventModel,
  ClientSecretRemovedEventModel,
  ErrorCode,
  JsErrorResponse,
  RedirectUrisUpdatedEventModel,
  TeamMemberAddedEventModel,
  TeamMemberRemovedEventModel}

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

  def teamMemberAdded(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[TeamMemberAddedEventModel] { event =>
      service.captureTeamMemberAddedEvent(event) map mapResult recover recovery
    }
  }

  def teamMemberRemoved(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[TeamMemberRemovedEventModel] { event =>
      service.captureTeamMemberRemovedEvent(event) map mapResult recover recovery
    }
  }

  def clientSecretAdded(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ClientSecretAddedEventModel] { event =>
      service.captureClientSecretAddedEvent(event) map mapResult recover recovery
    }
  }

  def clientSecretRemoved(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ClientSecretRemovedEventModel] { event =>
      service.captureClientSecretRemovedEvent(event) map mapResult recover recovery
    }
  }

  def redirectUrisUpdated(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[RedirectUrisUpdatedEventModel] { event =>
      service.captureRedirectUrisUpdatedEvent(event) map mapResult recover recovery
    }
  }

  def apiSubscribed(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApiSubscribedEventModel] { event =>
      service.captureApiSubscribedEvent(event) map mapResult recover recovery
    }
  }

  def apiUnsubscribed(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApiUnsubscribedEventModel] { event =>
      service.captureApiUnsubscribedEvent(event) map mapResult recover recovery
    }
  }

  override protected def withJsonBody[T]
  (f: T => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Future[Result] = {
    withJson(request.body)(f)
  }

  private def withJson[T](json: JsValue)(f: T => Future[Result])(implicit m: Manifest[T], reads: Reads[T]): Future[Result] = {
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