package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.support.ServerBaseISpec

class ApplicationEventsControllerISpec extends ServerBaseISpec {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port/api-platform-events/application-events"

  val wsClient = app.injector.instanceOf[WSClient]

  def doGet(path: String): WSResponse = {
    wsClient
      .url(s"$url$path")
      .get()
      .futureValue
  }

  def doPost(path: String, body: String, headers: (String, String)): WSResponse = {
    wsClient
      .url(s"$url$path")
      .withHttpHeaders(headers)
      .post(body)
      .futureValue
  }

  "ApplicationEventsController" when {

    "GET /helloworld" should {
      "respond with 200" in {
        val result = doGet("/helloworld")
        result.status shouldBe 200
        result.body shouldBe "hello world application"
      }
    }

    "POST /teamMemberAdded" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/teamMemberAdded", "{\n\t\"applicationId\": \"akjhjkhjshjkhksaih\",\n\t\"eventTimeStamp\": 1585830790,\n\t\"teamMemberEmail\": \"bob@bob.com\",\n\t\"teamMemberRole\": \"ADMIN\"\n}", "Content-Type"-> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/teamMemberAdded", "i'm not JSON", "Content-Type"-> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 400 when contentType header is missing" in {
        val result = doPost("/teamMemberAdded", "{\"SomeJson\": \"hello\"}", "somHeader"-> "someValue" )
        result.status shouldBe 400
        result.body shouldBe ""
      }

      "respond with 400 when contentType header isn't JSON" in {
        val result = doPost("/teamMemberAdded", "{\"SomeJson\": \"hello\"}", "Content-Type"-> "application/xml")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }
    }
  }
}
