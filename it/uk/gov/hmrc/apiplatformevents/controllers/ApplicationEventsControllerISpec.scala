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
        val result = doPost("/teamMemberAdded", validTeamMemberJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/teamMemberAdded", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/teamMemberAdded", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/teamMemberAdded", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }

    "POST /teamMemberRemoved" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/teamMemberRemoved", validTeamMemberJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/teamMemberRemoved", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/teamMemberRemoved", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/teamMemberRemoved", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }

    "POST /clientSecretAdded" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/clientSecretAdded", validClientSecretJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/clientSecretAdded", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/clientSecretAdded", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/clientSecretAdded", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }

    "POST /clientSecretRemoved" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/clientSecretRemoved", validClientSecretJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/clientSecretRemoved", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/clientSecretRemoved", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/clientSecretRemoved", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }

    "POST /redirectUrisUpdated" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/redirectUrisUpdated", validRedirectUrisUpdatedJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/redirectUrisUpdated", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/redirectUrisUpdated", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/redirectUrisUpdated", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }

    "POST /apiSubscribed" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/apiSubscribed", validApiSubscriptionJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/apiSubscribed", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/apiSubscribed", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/apiSubscribed", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }

    "POST /apiUnsubscribed" should {
      "respond with 201 when valid json is sent" in {
        val result = doPost("/apiUnsubscribed", validApiSubscriptionJsonBody, "Content-Type" -> "application/json")
        result.status shouldBe 201
        result.body shouldBe ""
      }

      "respond with 400 when invalid json is sent" in {
        val result = doPost("/apiUnsubscribed", "i'm not JSON", "Content-Type" -> "application/json")
        result.status shouldBe 400
        result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      }

      "respond with 415 when contentType header is missing" in {
        val result = doPost("/apiUnsubscribed", "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      "respond with 415 when contentType header isn't JSON" in {
        val result = doPost("/apiUnsubscribed", "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml")
        result.status shouldBe 415
        result.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }
  }
}
