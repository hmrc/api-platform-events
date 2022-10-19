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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatformevents.models.JsonRequestFormatters._
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

@Singleton
class ApplicationEventsController @Inject()(
  val env: Environment,
  service: ApplicationEventsService,
  playBodyParsers: PlayBodyParsers,
  cc: ControllerComponents
)(
  implicit val configuration: Configuration,
  ec: ExecutionContext
) extends BackendController(cc) with ApplicationLogger {

 
  def handleEvent(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApplicationEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def ppnsCallbackUriUpdated(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[PpnsCallBackUriUpdatedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  @deprecated("please pass CollaboratorAdded to handleEvent endpoint")
  def teamMemberAdded(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[TeamMemberAddedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  @deprecated("please pass CollaboratorRemoved to handleEvent endpoint")
  def teamMemberRemoved(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[TeamMemberRemovedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  @deprecated("please pass ClientSecretAddedV2 to handleEvent endpoint")
  def clientSecretAdded(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ClientSecretAddedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  @deprecated("please pass ClientSecretRemovedV2 to handleEvent endpoint")
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

  @deprecated("please pass ApiSubscribedV2 to handleEvent endpoint")
  def apiSubscribed(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApiSubscribedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  @deprecated("please pass ApiUnsubscribedV2 to handleEvent endpoint")
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
        errs.foreach(err=> logger.info(err._2.mkString(" ")))
        Future.successful(UnprocessableEntity(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, JsError.toJson(errs))))
      case Failure(e) =>
        logger.info(s"Error with request Json ${e.getMessage}")
        Future.successful(UnprocessableEntity(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, e.getMessage)))
    }
  }

  private def mapResult(result: Boolean): Status = {
    if (result) {
      Created
    } else {
       logger.error("An unexpected error occurred: false returned from create operation")
      InternalServerError
    }
  }

  private def recovery: PartialFunction[Throwable, Result] = {
    case NonFatal(e) => logger.error("An unexpected error occurred:", e)
      InternalServerError
  }
}