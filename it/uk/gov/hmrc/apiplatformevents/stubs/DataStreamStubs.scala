package uk.gov.hmrc.apiplatformevents.stubs

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.apiplatformevents.services.ApiPlatformEventsWithMongodbEvent.ApiPlatformEventsWithMongodbEvent
import uk.gov.hmrc.apiplatformevents.support.WireMockSupport

trait DataStreamStubs extends Eventually {
  me: WireMockSupport =>

  override implicit val patienceConfig =
    PatienceConfig(scaled(Span(5, Seconds)), scaled(Span(500, Millis)))

  def verifyAuditRequestSent(
    count: Int,
    event: ApiPlatformEventsWithMongodbEvent,
    tags: Map[String, String] = Map.empty,
    detail: Map[String, String] = Map.empty): Unit =
    eventually {
      verify(
        count,
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(similarToJson(s"""{
          |  "auditSource": "api-platform-events-with-mongodb",
          |  "auditType": "$event",
          |  "tags": ${Json.toJson(tags)},
          |  "detail": ${Json.toJson(detail)}
          |}"""))
      )
    }

  def verifyAuditRequestNotSent(event: ApiPlatformEventsWithMongodbEvent): Unit =
    eventually {
      verify(
        0,
        postRequestedFor(urlPathEqualTo(auditUrl))
          .withRequestBody(similarToJson(s"""{
          |  "auditSource": "api-platform-events-with-mongodb",
          |  "auditType": "$event"
          |}"""))
      )
    }

  def givenAuditConnector(): Unit = {
    stubFor(post(urlPathEqualTo(auditUrl)).willReturn(aResponse().withStatus(200)))
    stubFor(post(urlPathEqualTo(auditUrl + "/merged")).willReturn(aResponse().withStatus(200)))
  }

  private def auditUrl = "/write/audit"

  private def similarToJson(value: String) = equalToJson(value.stripMargin, true, true)

}
