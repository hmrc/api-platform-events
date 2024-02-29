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

package uk.gov.hmrc.apiplatformevents.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import play.api.libs.json.Json

import uk.gov.hmrc.apiplatformevents.connectors.SendEmailRequest

trait EmailService {
  private val url = "/hmrc/email"

  def primeEmailEndpoint(status: Int, responseBody: String = ""): StubMapping =
    stubFor(
      post(urlPathEqualTo(url))
        .willReturn(
          aResponse()
            .withBody(responseBody)
            .withStatus(status)
        )
    )

  def verifyRequestBody(expectedRequest: SendEmailRequest) =
    verify(
      postRequestedFor(urlPathEqualTo(url))
        .withRequestBody(equalToJson(Json.toJson(expectedRequest).toString()))
    )
}
