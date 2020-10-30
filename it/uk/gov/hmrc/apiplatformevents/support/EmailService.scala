package uk.gov.hmrc.apiplatformevents.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait EmailService {
  private val url = "/hmrc/email"

  def primeEmailEndpoint(status : Int, body: String): StubMapping = {
    stubFor(post(urlPathEqualTo(url))
      .willReturn(
        aResponse()
        .withBody(body)
        .withStatus(status)
      )
    )
  }
}
