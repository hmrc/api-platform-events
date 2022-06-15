package uk.gov.hmrc.apiplatformevents.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.apiplatformevents.models.{ApplicationResponse, Collaborator, Role}

trait ThirdPartyApplicationService {
  private def applicationUrl(appId: String) = s"/application/$appId"

  def primeApplicationEndpoint(status : Int, body: String, applicationId: String): StubMapping = {
    stubFor(get(urlPathEqualTo(applicationUrl(applicationId)))
      .willReturn(
        aResponse()
        .withBody(body)
        .withStatus(status)
      )
    )
  }

  val appResponseWithAdmins = ApplicationResponse("app1", Set(Collaborator("some@one.com",Role.ADMINISTRATOR)))
}
