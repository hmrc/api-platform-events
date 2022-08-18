package uk.gov.hmrc.apiplatformevents.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.apiplatformevents.models.{ApplicationResponse, Collaborator, Role}
import uk.gov.hmrc.apiplatformevents.models.common.ApplicationId

trait ThirdPartyApplicationService {
  private def applicationUrl(appId: ApplicationId) = s"/application/${appId.value}"

  def primeApplicationEndpoint(status : Int, body: String, applicationId: ApplicationId): StubMapping = {
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
