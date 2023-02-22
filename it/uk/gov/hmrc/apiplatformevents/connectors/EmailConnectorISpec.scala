package uk.gov.hmrc.apiplatformevents.connectors

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.support.{EmailService, MetricsTestSupport, WireMockSupport}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

import java.time.format.DateTimeFormatter
import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import java.time.Instant
import java.time.ZoneOffset

class EmailConnectorISpec extends AsyncHmrcSpec with WireMockSupport with GuiceOneAppPerSuite with MetricsTestSupport with EmailService {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def commonStubs(): Unit = givenCleanMetricRegistry()

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
