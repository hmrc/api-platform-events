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

package uk.gov.hmrc.apiplatformevents.connectors

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig)(implicit val ec: ExecutionContext) {

  val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy")
  val timeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")

  def sendPpnsCallbackUrlChangedNotification(applicationName: String, dateTimeOfChange: DateTime, recipients: Set[String])
                                            (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    post(SendEmailRequest(
      recipients,
      "ppnsCallbackUrlChangedNotification",
      Map("applicationName" -> applicationName,
        "dateOfChange" -> dateTimeOfChange.toString(dateFormatter),
        "timeOfChange" -> dateTimeOfChange.toString(timeFormatter))))
  }

  private def post(payload: SendEmailRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient.POST[SendEmailRequest, HttpResponse](s"${appConfig.emailUrl}/hmrc/email", payload)
  }
}

case class SendEmailRequest(to: Set[String],
                            templateId: String,
                            parameters: Map[String, String],
                            force: Boolean = false,
                            auditData: Map[String, String] = Map.empty,
                            eventUrl: Option[String] = None)

object SendEmailRequest {
  implicit val sendEmailRequestFmt: OFormat[SendEmailRequest] = Json.format[SendEmailRequest]
}
