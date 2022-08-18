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
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common._
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import cats.data.ValidatedNec
import cats.implicits._
import cats.implicits._
import uk.gov.hmrc.apiplatformevents.models.common.EventType
import play.api.libs.json.Json
import JsonRequestFormatters._

object QueryController {
  case class QueryResponse(events: Seq[ApplicationEvent])

  object QueryResponse {
    implicit val format = Json.format[QueryResponse]
  }
}

@Singleton
class QueryController @Inject()(
  val env: Environment,
  service: ApplicationEventsService,
  playBodyParsers: PlayBodyParsers,
  cc: ControllerComponents
)(
  implicit val configuration: Configuration,
  ec: ExecutionContext
) extends BackendController(cc) with ApplicationLogger {

  import QueryController._

  type ValidationResult[A] = ValidatedNec[String, A]


  def validateYear(in: String): ValidationResult[Int] = {
    try {
      in.toInt.validNec
    } catch {
      case e: Exception => (s"$in is not a valid year").invalidNec
    }
  }

  def validateEventType(in: String): ValidationResult[EventType] = {
    EventType.values.find(et => et.entryName == in.toUpperCase).toValidNec(s"EventType $in is invalid")
  }

  def validateActor(in: String): ValidationResult[String] = {
    in.validNec
  }

  def extractOption[A](param:String)(fn: String => ValidationResult[A])(implicit request: Request[_]) = {
    request.queryString(param)
    .headOption
    .fold[ValidationResult[Option[A]]](None.validNec)(fn(_).map(Some(_)))
  }

  def query(applicationId: ApplicationId)(year: Option[Int], eventType: Option[EventType], actor: Option[String]): Future[Seq[ApplicationEvent]] = {
    service.fetchEventsBy(applicationId, year, eventType, actor)
  }

  def asResult(in: Future[Seq[ApplicationEvent]]): Future[Result] = {
    in.map(evts => Ok(Json.toJson(QueryResponse(evts))))
  }

  def queryDispatcher(applicationId: ApplicationId) = Action.async { implicit request =>

    val yearV = extractOption("year")(validateYear)
    
    val eventTypeV = extractOption("eventType")(validateEventType)

    val actorV = extractOption("actor")(validateActor)

    (yearV, eventTypeV, actorV)
    .mapN(query(applicationId))
    .map(asResult)
    .leftMap(errs => Future.successful(BadRequest(errs.toList.mkString(","))))
    .merge
  }
}