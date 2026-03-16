/*
 * Copyright 2026 HM Revenue & Customs
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

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatformevents.models.{ErrorCode, JsErrorResponse}
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger

abstract class AbstractEventsController(
    cc: ControllerComponents
) extends BackendController(cc)
    with ApplicationLogger {

  override protected def withJsonBody[T](f: T => Future[Result])(implicit request: Request[JsValue], c: ClassTag[T], reads: Reads[T]): Future[Result] = {
    withJson(request.body)(f)
  }

  protected def withJson[T](json: JsValue)(f: T => Future[Result])(implicit reads: Reads[T]): Future[Result] = {
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

  protected def mapResult(result: Boolean): Status = {
    if (result) {
      Created
    } else {
      logger.error("An unexpected error occurred: false returned from create operation")
      InternalServerError
    }
  }

  protected def recovery: PartialFunction[Throwable, Result] = { case NonFatal(e) =>
    logger.error("An unexpected error occurred:", e)
    InternalServerError
  }
}
