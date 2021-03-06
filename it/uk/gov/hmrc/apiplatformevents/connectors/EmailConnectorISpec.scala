package uk.gov.hmrc.apiplatformevents.connectors

import org.joda.time.DateTime
import org.joda.time.DateTime.now
import org.joda.time.DateTimeZone.UTC
import org.joda.time.format.DateTimeFormat
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.support.{EmailService, MetricsTestSupport, WireMockSupport}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

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
    val applicationName: String = "foobar app"
    val dateTimeOfChange: DateTime = now(UTC)
    val recipients: Set[String] = Set("john.doe@example.com")

    val expectedRequestBody = SendEmailRequest(recipients,
      "ppnsCallbackUrlChangedNotification",
      Map("applicationName" -> applicationName,
        "dateOfChange" -> dateTimeOfChange.toString(DateTimeFormat.forPattern("dd MMMM yyyy")),
        "timeOfChange" -> dateTimeOfChange.toString(DateTimeFormat.forPattern("HH:mm"))))

    "send the notification using the email service" in new SetUp() {
      val expectedHttpStatus: Int = OK
      primeEmailEndpoint(expectedHttpStatus)

      val result: HttpResponse = await(objInTest.sendPpnsCallbackUrlChangedNotification(applicationName, dateTimeOfChange, recipients))

      result.status shouldBe expectedHttpStatus
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
