/*
 * Copyright 2024 HM Revenue & Customs
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

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}

import uk.gov.hmrc.apiplatformevents.support.{EmailService, WireMockSupport}
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

class EmailConnectorISpec extends AsyncHmrcSpec with WireMockSupport with GuiceOneAppPerSuite with EmailService {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.email.port" -> wireMockPort
      )

  trait SetUp {
    val objInTest: EmailConnector = app.injector.instanceOf[EmailConnector]
  }

  "sendPpnsCallbackUrlChangedNotification" should {
    val applicationName: String   = "foobar app"
    val dateTimeOfChange: Instant = Instant.now()
    val recipients                = Set("john.doe@example.com").map(LaxEmailAddress(_))

    val expectedRequestBody = SendEmailRequest(
      recipients,
      "ppnsCallbackUrlChangedNotification",
      Map(
        "applicationName" -> applicationName,
        "dateOfChange"    -> DateTimeFormatter.ofPattern("dd MMMM yyyy").format(dateTimeOfChange.atZone(ZoneOffset.UTC)),
        "timeOfChange"    -> DateTimeFormatter.ofPattern("HH:mm").format(dateTimeOfChange.atZone(ZoneOffset.UTC))
      )
    )

    "send the notification using the email service" in new SetUp() {
      primeEmailEndpoint(OK)

      val result: HttpResponse = await(objInTest.sendPpnsCallbackUrlChangedNotification(applicationName, dateTimeOfChange, recipients))

      result.status shouldBe OK
      verifyRequestBody(expectedRequestBody)
    }

    "return failed Future if the email service returns a 404" in new SetUp {
      primeEmailEndpoint(NOT_FOUND)

      intercept[NotFoundException] {
        await(objInTest.sendPpnsCallbackUrlChangedNotification(applicationName, dateTimeOfChange, recipients))
      }

      verifyRequestBody(expectedRequestBody)
    }
  }
}
