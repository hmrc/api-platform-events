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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatform.modules.applications.core.domain.models.{ApplicationName, ApplicationWithCollaboratorsFixtures}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import uk.gov.hmrc.apiplatformevents.models.ApplicationResponse
import uk.gov.hmrc.apiplatformevents.support.{ThirdPartyApplicationService, WireMockSupport}
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

class ThirdPartyApplicationConnectorISpec
    extends AsyncHmrcSpec
    with WireMockSupport
    with GuiceOneAppPerSuite
    with ThirdPartyApplicationService
    with ApplicationWithCollaboratorsFixtures {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.third-party-application.port" -> wireMockPort
      )

  trait SetUp {
    val objInTest: ThirdPartyApplicationConnector = app.injector.instanceOf[ThirdPartyApplicationConnector]
  }

  "getApplicationName" should {
    val applicationId = ApplicationId.random
    val expectedApp   = ApplicationResponse("foobar app", Set.empty)

    "retrieve application record based on provided clientId" in new SetUp() {
      val jsonResponse: String = Json.prettyPrint(
        Json.toJson(
          standardApp
            .withId(applicationId)
            .withName(ApplicationName(expectedApp.name))
            .withCollaborators()
        )
      )
      primeApplicationEndpoint(OK, jsonResponse, applicationId)

      val result: ApplicationResponse = await(objInTest.getApplication(applicationId))

      result shouldBe expectedApp
    }

    "return failed Future if TPA returns a 404" in new SetUp {
      primeApplicationEndpoint(NOT_FOUND, "", applicationId)

      intercept[UpstreamErrorResponse] {
        await(objInTest.getApplication(applicationId))
      }.statusCode shouldBe NOT_FOUND
    }
  }
}
