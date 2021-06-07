package uk.gov.hmrc.apiplatformevents.connectors

import java.util.UUID.randomUUID

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.models.ApplicationResponse
import uk.gov.hmrc.apiplatformevents.support.{MetricsTestSupport, ThirdPartyApplicationService, WireMockSupport}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

class ThirdPartyApplicationConnectorISpec extends AsyncHmrcSpec with WireMockSupport with GuiceOneAppPerSuite with ScalaFutures with MetricsTestSupport with ThirdPartyApplicationService {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def commonStubs(): Unit = givenCleanMetricRegistry()

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
    val applicationId = randomUUID.toString
    val expectedApp = ApplicationResponse("foobar app", Set.empty)

    "retrieve application record based on provided clientId" in new SetUp() {
      val jsonResponse: String = raw"""{"id":  "$applicationId", "name": "${expectedApp.name}", "collaborators": []}"""
      primeApplicationEndpoint(OK, jsonResponse, applicationId)

      val result: ApplicationResponse = await(objInTest.getApplication(applicationId))

      result shouldBe expectedApp
    }

    "return failed Future if TPA returns a 404" in new SetUp {
      primeApplicationEndpoint(NOT_FOUND, "", applicationId)

      intercept[NotFoundException] {
        await(objInTest.getApplication(applicationId))
      }
    }
  }
}
