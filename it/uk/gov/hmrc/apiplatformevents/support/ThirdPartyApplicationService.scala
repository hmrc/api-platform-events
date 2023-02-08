package uk.gov.hmrc.apiplatformevents.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId
import uk.gov.hmrc.apiplatformevents.models.ApplicationResponse
import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.Collaborators
import uk.gov.hmrc.apiplatform.modules.developers.domain.models.UserId

trait ThirdPartyApplicationService {
  private def applicationUrl(appId: ApplicationId) = s"/application/${appId.value}"

  def primeApplicationEndpoint(status: Int, body: String, applicationId: ApplicationId): StubMapping = {
    stubFor(
      get(urlPathEqualTo(applicationUrl(applicationId)))
        .willReturn(
          aResponse()
            .withBody(body)
            .withStatus(status)
        )
    )
  }

  val appResponseWithAdmins = ApplicationResponse("app1", Set(Collaborators.Administrator(UserId.random, LaxEmailAddress("some@one.com"))))
}
