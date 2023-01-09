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

package uk.gov.hmrc.apiplatformevents.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.http.Status.NOT_FOUND

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.NotFoundException

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.LaxEmailAddress

@Singleton
class EmailConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit val ec: ExecutionContext) {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  def sendPpnsCallbackUrlChangedNotification(applicationName: String, dateTimeOfChange: LocalDateTime, recipients: Set[LaxEmailAddress])(implicit
      hc: HeaderCarrier
  ): Future[HttpResponse] = {
    post(
      SendEmailRequest(
        recipients,
        "ppnsCallbackUrlChangedNotification",
        Map("applicationName" -> applicationName, "dateOfChange" -> dateTimeOfChange.format(dateFormatter), "timeOfChange" -> dateTimeOfChange.format(timeFormatter))
      )
    )
  }

  private def post(payload: SendEmailRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient
      .POST[SendEmailRequest, HttpResponse](s"${appConfig.emailUrl}/hmrc/email", payload)
      .map { response =>
        response.status match {
          case NOT_FOUND => throw new NotFoundException("Endpoint for sending email was not found")
          case _         => response
        }
      }
  }
}

case class SendEmailRequest(
    to: Set[LaxEmailAddress],
    templateId: String,
    parameters: Map[String, String],
    force: Boolean = false,
    auditData: Map[String, String] = Map.empty,
    eventUrl: Option[String] = None
)

object SendEmailRequest {
  import uk.gov.hmrc.apiplatform.modules.events.applications.domain.services.CommonJsonFormatters._
  implicit val sendEmailRequestFmt: OFormat[SendEmailRequest] = Json.format[SendEmailRequest]
}
