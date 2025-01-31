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
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.EventsInterServiceCallJsonFormatters
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatformevents.models.{ErrorCode, JsErrorResponse}
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger

@Singleton
class ApplicationEventsController @Inject() (
    val env: Environment,
    service: ApplicationEventsService,
    playBodyParsers: PlayBodyParsers,
    cc: ControllerComponents
)(implicit
    val configuration: Configuration,
    ec: ExecutionContext
) extends BackendController(cc)
    with ApplicationLogger {

  import EventsInterServiceCallJsonFormatters._

  def handleEvent(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApplicationEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  def ppnsCallbackUriUpdated(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    withJsonBody[ApplicationEvents.PpnsCallBackUriUpdatedEvent] { event =>
      service.captureEvent(event) map mapResult recover recovery
    }
  }

  // Note that this a test-only route, for use in QA only
  def deleteEventsForApplication(applicationId: ApplicationId): Action[AnyContent] = Action.async { _ =>
    def success(numberOfRecordsDeleted: Long) = {
      logger.info(s"test-only: Successfully deleted $numberOfRecordsDeleted event records for application $applicationId")
      NoContent
    }
    service.deleteEventsForApplication(applicationId).map(success)
  }

  override protected def withJsonBody[T](f: T => Future[Result])(implicit request: Request[JsValue], c: ClassTag[T], reads: Reads[T]): Future[Result] = {
    withJson(request.body)(f)
  }

  private def withJson[T](json: JsValue)(f: T => Future[Result])(implicit reads: Reads[T]): Future[Result] = {
    Try(json.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs))         =>
        errs.foreach(err => logger.info(err._2.mkString(" ")))
        Future.successful(UnprocessableEntity(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, JsError.toJson(errs))))
      case Failure(e)                     =>
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

  private def recovery: PartialFunction[Throwable, Result] = { case NonFatal(e) =>
    logger.error("An unexpected error occurred:", e)
    InternalServerError
  }
}
