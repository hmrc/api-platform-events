package uk.gov.hmrc.apiplatformevents.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatformevents.connectors.SendEmailRequest

trait EmailService {
  private val url = "/hmrc/email"

  def primeEmailEndpoint(status : Int, responseBody: String = ""): StubMapping =
    stubFor(post(urlPathEqualTo(url))
      .willReturn(
        aResponse()
        .withBody(responseBody)
        .withStatus(status)
      )
    )

  def verifyRequestBody(expectedRequest: SendEmailRequest) =
    verify(postRequestedFor(urlPathEqualTo(url))
      .withRequestBody(equalToJson(Json.toJson(expectedRequest).toString())))
}
