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
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import cats.data.ValidatedNec
import cats.implicits._
import uk.gov.hmrc.apiplatformevents.models.common.EventType
import play.api.libs.json.Json
import JsonRequestFormatters._

object QueryEventsController {
  case class QueryResponse(events: Seq[ApplicationEvent])

  object QueryResponse {
    implicit val format = Json.format[QueryResponse]
  }

  type ValidationResult[A] = ValidatedNec[String, A]

  def validateYear(in: String): ValidationResult[Int] = {
    try {
      (in.toInt).validNec
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
    request.getQueryString(param)
    .fold[ValidationResult[Option[A]]](None.validNec)(fn(_).map(Some(_)))
  }

  def extractOptions(implicit request: Request[_]): (ValidationResult[Option[Int]], ValidationResult[Option[EventType]], ValidationResult[Option[String]]) = {
    val yearV = extractOption("year")(validateYear)
    
    val eventTypeV = extractOption("eventType")(validateEventType)

    val actorV = extractOption("actor")(validateActor)

    (yearV, eventTypeV, actorV)
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

  implicit val orderEvents: Ordering[ApplicationEvent] = new Ordering[ApplicationEvent]() {
    override def compare(x: ApplicationEvent, y: ApplicationEvent): Int = x.eventDateTime.compareTo(y.eventDateTime)
  }

  private def query(applicationId: String)(year: Option[Int], eventType: Option[EventType], actor: Option[String]): Future[Seq[ApplicationEvent]] = {
    service.fetchEventsBy(applicationId, year, eventType, actor)
  }

  def queryDispatcher(applicationId: String) = Action.async { implicit request =>
    def asResult(in: Future[Seq[ApplicationEvent]]): Future[Result] = {
      in.map(evts => Ok(Json.toJson(QueryResponse(evts.sorted))))
    }

    val (yearV, eventTypeV, actorV) = extractOptions(request)

    (yearV, eventTypeV, actorV)
    .mapN(query(applicationId))
    .map(asResult)
    .leftMap(errs => Future.successful(BadRequest(errs.toList.mkString(","))))
    .merge
  }

  def queryValues(applicationId: String) = Action.async { _ =>
    service.fetchEventQueryValues(applicationId)
    .map[Result](_.fold(NotFound(""))(qv => Ok(Json.toJson(qv))))
  }
}