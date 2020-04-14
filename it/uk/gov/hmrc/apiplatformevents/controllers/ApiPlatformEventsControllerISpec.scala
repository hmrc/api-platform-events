package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.support.ServerBaseISpec

class ApiPlatformEventsControllerISpec extends ServerBaseISpec {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port"

  val wsClient = app.injector.instanceOf[WSClient]

  def entity(): WSResponse =
    wsClient
      .url(s"$url/helloworld")
      .get()
      .futureValue

  "ApiPlatformEventsController" when {

    "GET /helloworld" should {
      "respond with 200" in {
        val result = entity()
        result.status shouldBe 200
        result.body shouldBe "hello world"
      }
    }
  }
}
