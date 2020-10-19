package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

class ApplicationEventsControllerISpec extends ServerBaseISpec  with AuditService with  BeforeAndAfterEach {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port/application-events"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  override def beforeEach(): Unit = {
   super.beforeEach()
    primeAuditService()
  }

  val validTeamMemberJsonBody: String =
    raw"""{"applicationId": "akjhjkhjshjkhksaih",
         |"eventDateTime": "2014-01-01T13:13:34.441Z",
         |"actor": { "id": "123454654", "actorType": "GATEKEEPER" },
         |"teamMemberEmail": "bob@bob.com",
         |"teamMemberRole": "ADMIN"}""".stripMargin

  val validClientSecretJsonBody: String =
    raw"""{"applicationId": "akjhjkhjshjkhksaih",
         |"eventDateTime": "2014-01-01T13:13:34.441Z",
         |"actor": { "id": "123454654", "actorType": "GATEKEEPER" },
         |"clientSecretId": "abababab"}""".stripMargin

  val validRedirectUrisUpdatedJsonBody: String =
    raw"""{"applicationId": "akjhjkhjshjkhksaih",
         |"eventDateTime": "2014-01-01T13:13:34.441Z",
         |"actor": { "id": "123454654", "actorType": "GATEKEEPER" },
         |"oldRedirectUris": "oldrdu",
         |"newRedirectUris": "newrdu"}""".stripMargin

  val validApiSubscriptionJsonBody: String =
    raw"""{"applicationId": "akjhjkhjshjkhksaih",
         |"eventDateTime": "2014-01-01T13:13:34.441Z",
         |"actor": { "id": "123454654", "actorType": "GATEKEEPER" },
         |"context": "apicontext",
         |"version": "1.0"}""".stripMargin

  val validPpnsCallBackUpdatedJsonBody: String =
    raw"""{"applicationId": "akjhjkhjshjkhksaih",
         |"eventDateTime": "2014-01-01T13:13:34.441Z",
         |"actor": { "id": "123454654", "actorType": "GATEKEEPER" },
         |"boxId": "someBoxId",
         |"context": "apicontext",
         |"version": "1.0",
         |"oldCallbackUrl": "oldUrl",
         |"newCallbackUrl": "newUrl"}""".stripMargin

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

    "POST /teamMemberAdded" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/teamMemberAdded", validTeamMemberJsonBody)
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/teamMemberAdded")
      }
    }

    "POST /teamMemberRemoved" should {
      "respond with 201 when valid json is sent" in {
         testSuccessScenario("/teamMemberRemoved", validTeamMemberJsonBody)
      }

     "handle error scenarios correctly" in {
        testErrorScenarios("/teamMemberRemoved")
      }
    }

    "POST /clientSecretAdded" should {
      "respond with 201 when valid json is sent" in {
         testSuccessScenario("/clientSecretAdded", validClientSecretJsonBody)
      }

     "handle error scenarios correctly" in {
        testErrorScenarios("/clientSecretAdded")
      }
    }

    "POST /clientSecretRemoved" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/clientSecretRemoved", validClientSecretJsonBody)
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/clientSecretRemoved")
      }
    }

    "POST /redirectUrisUpdated" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/redirectUrisUpdated", validRedirectUrisUpdatedJsonBody)
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/redirectUrisUpdated")
      }
    }

    "POST /apiSubscribed" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/apiSubscribed", validApiSubscriptionJsonBody)
      }

     "handle error scenarios correctly" in {
        testErrorScenarios("/apiSubscribed")
      }
    }

    "POST /apiUnsubscribed" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/apiUnsubscribed", validApiSubscriptionJsonBody)
      }

     "handle error scenarios correctly" in {
        testErrorScenarios("/apiUnsubscribed")
      }
    }

    "POST /ppnsCallbackUriUpdated" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody)
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/ppnsCallbackUriUpdated")
      }

    }

    def testSuccessScenario(uriToTest: String, bodyString: String):Unit = {
         val result = doPost(uriToTest, bodyString, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
    }


    def testErrorScenarios(uriToTest: String): Unit ={
      val result = doPost(uriToTest, "i'm not JSON", "Content-Type" -> "application/json")
      withClue("should respond with 400 when invalid json is sent"){
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      val result2 = doPost(uriToTest, "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
      withClue("should respond with 415 when contentType header is missing"){
        result2.status shouldBe 415
        result2.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      val result3 = doPost(uriToTest, "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
      withClue("should respond with 415 when contentType header isn't JSON") {
        result3.status shouldBe 415
        result3.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }
  }
}
