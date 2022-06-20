package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.EventId
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

import java.util.UUID
import java.{util => ju}
import scala.concurrent.Future

class ApplicationEventsControllerISpec extends ServerBaseISpec  with AuditService with BeforeAndAfterEach {

  this: Suite with ServerProvider =>

  def repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    primeAuditService()
    await(repo.collection.drop().toFuture())
  }

  val url = s"http://localhost:$port/application-events"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val eventId: UUID = EventId.random.value
  val applicationId: String = ju.UUID.randomUUID.toString
  val actorId = "123454654"
  val actorTypeGK = "GATEKEEPER"
  val eventDateTimeString = "2014-01-01T13:13:34.441"

  def validTeamMemberJsonBody(teamMemberEmail: String, teamMemberRole: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"teamMemberEmail": "$teamMemberEmail",
         |"teamMemberRole": "$teamMemberRole"}""".stripMargin

  def validClientSecretJsonBody(clientSecretId: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"clientSecretId": "$clientSecretId"}""".stripMargin

  def validRedirectUrisUpdatedJsonBody(oldRedirectUri: String, newRedirectUri: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"oldRedirectUris": "$oldRedirectUri",
         |"newRedirectUris": "$newRedirectUri"}""".stripMargin

  def validApiSubscriptionJsonBody(apiContext: String, apiVersion: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"context": "$apiContext",
         |"version": "$apiVersion"}""".stripMargin

  def validPpnsCallBackUpdatedJsonBody(boxId: String, boxName: String, oldCallbackUrl: String, newCallbackUrl: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"boxId": "$boxId",
         |"boxName": "$boxName",
         |"oldCallbackUrl": "$oldCallbackUrl",
         |"newCallbackUrl": "$newCallbackUrl"}""".stripMargin

  def validProductionAppNameChangedJsonBody(oldAppName: String, newAppName: String, requestingAdminName: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"oldAppName": "$oldAppName",
         |"newAppName": "$newAppName",
         |"requestingAdminName": "$requestingAdminName"}""".stripMargin

  def doGet(path: String): Future[WSResponse] = {
    wsClient
      .url(s"$url$path")
      .get()
  }

  def doPost(path: String, body: String, headers: (String, String)): Future[WSResponse] = {
    wsClient
      .url(s"$url$path")
      .withHttpHeaders(headers)
      .post(body)
  }

  def checkCommonEventValues[A <: OldApplicationEvent](event: A): Unit = {
    event.applicationId shouldBe applicationId
    event.eventDateTime.toString shouldBe eventDateTimeString
    event.actor.id shouldBe actorId
    event.actor.actorType.toString shouldBe actorTypeGK
  }

  "ApplicationEventsController" when {

    "POST /teamMemberAdded" should {
      "respond with 201 when valid json is sent" in {
        val teamMemberEmail = "bob@bob.com"
        val adminRole = "ADMIN"

        testSuccessScenario("/teamMemberAdded", validTeamMemberJsonBody(teamMemberEmail, adminRole))
        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[TeamMemberAddedEvent]

        checkCommonEventValues(event)
        event.teamMemberEmail shouldBe teamMemberEmail
        event.teamMemberRole shouldBe adminRole
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/teamMemberAdded")
      }
    }

    "POST /teamMemberRemoved" should {
      "respond with 201 when valid json is sent" in {
        val teamMemberEmail = "bob@bob.com"
        val adminRole = "ADMIN"

        testSuccessScenario("/teamMemberRemoved", validTeamMemberJsonBody(teamMemberEmail, adminRole))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[TeamMemberRemovedEvent]

        checkCommonEventValues(event)
        event.teamMemberEmail shouldBe teamMemberEmail
        event.teamMemberRole shouldBe adminRole
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/teamMemberRemoved")
      }
    }

    "POST /clientSecretAdded" should {
      "respond with 201 when valid json is sent" in {
        val clientSecretId = ju.UUID.randomUUID().toString

        testSuccessScenario("/clientSecretAdded", validClientSecretJsonBody(clientSecretId))


        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ClientSecretAddedEvent]

        checkCommonEventValues(event)
        event.clientSecretId shouldBe clientSecretId

      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/clientSecretAdded")
      }
    }

    "POST /clientSecretRemoved" should {
      "respond with 201 when valid json is sent" in {
        val clientSecretId = ju.UUID.randomUUID().toString

        testSuccessScenario("/clientSecretRemoved", validClientSecretJsonBody(clientSecretId))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ClientSecretRemovedEvent]

        checkCommonEventValues(event)
        event.clientSecretId shouldBe clientSecretId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/clientSecretRemoved")
      }
    }

    "POST /redirectUrisUpdated" should {
      "respond with 201 when valid json is sent" in {
        val oldRedirectUri = "oldrdu"
        val newRedirectUri = "newrdu"

        testSuccessScenario("/redirectUrisUpdated", validRedirectUrisUpdatedJsonBody(oldRedirectUri, newRedirectUri))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[RedirectUrisUpdatedEvent]

        checkCommonEventValues(event)
        event.oldRedirectUris shouldBe oldRedirectUri
        event.newRedirectUris shouldBe newRedirectUri
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/redirectUrisUpdated")
      }
    }

    "POST /apiSubscribed" should {
      "respond with 201 when valid json is sent" in {
        val apiContext = "apicontext"
        val apiVersion = "1.0"

        testSuccessScenario("/apiSubscribed", validApiSubscriptionJsonBody(apiContext, apiVersion))
        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApiSubscribedEvent]

        checkCommonEventValues(event)
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/apiSubscribed")
      }
    }

    "POST /apiUnsubscribed" should {
      "respond with 201 when valid json is sent" in {
        val apiContext = "apicontext"
        val apiVersion = "1.0"

        testSuccessScenario("/apiUnsubscribed", validApiSubscriptionJsonBody(apiContext, apiVersion))
        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApiUnsubscribedEvent]

        checkCommonEventValues(event)
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/apiUnsubscribed")
      }
    }

    "POST /ppnsCallbackUriUpdated" should {
      "respond with 201 when valid json is sent" in {
        val boxId = ju.UUID.randomUUID().toString
        val boxName = "some##box##name"
        val oldCallbackUrl = "https://foo.bar/baz"
        val newCallbackUrl = "https://foo.bar/bazbazbaz"

        testSuccessScenario("/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody(boxId, boxName, oldCallbackUrl, newCallbackUrl))

        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[PpnsCallBackUriUpdatedEvent]

        checkCommonEventValues(event)
        event.boxId shouldBe boxId
        event.oldCallbackUrl shouldBe oldCallbackUrl
        event.newCallbackUrl shouldBe newCallbackUrl
        event.boxName shouldBe boxName
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/ppnsCallbackUriUpdated")
      }

    }

//    "POST /productionAppNameChanged" should {
//      "respond with 201 when valid json is sent" in {
//        val oldAppName = "old name"
//        val newAppName = "new name"
//        val requestingAdminName = "mrs admin"
//
//        testSuccessScenario("/productionAppNameChanged", validProductionAppNameChangedJsonBody(oldAppName, newAppName, requestingAdminName))
//
//        val results =await(repo.collection.find().toFuture())
//        results.size shouldBe 1
//        val event = results.head.asInstanceOf[ProductionAppNameChangedEvent]
//
//        checkCommonEventValues(event)
//        event.oldAppName shouldBe oldAppName
//        event.newAppName shouldBe newAppName
//        event.requestingAdminName shouldBe requestingAdminName
//      }
//
//      "handle error scenarios correctly" in {
//        testErrorScenarios("/productionAppNameChanged")
//      }
//
//    }

    def testSuccessScenario(uriToTest: String, bodyString: String): Unit = {
      val result = await(doPost(uriToTest, bodyString, "Content-Type" -> "application/json"))
      result.status shouldBe 201
      result.body shouldBe ""
    }


    def testErrorScenarios(uriToTest: String): Unit = {
      // val result = await(doPost(uriToTest, "i'm not JSON", "Content-Type" -> "application/json"))
      // withClue("should respond with 400 when invalid json is sent") {
      //   result.status shouldBe 400
      //   result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      // }

      val result2 = await(doPost(uriToTest, "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue"))
      withClue("should respond with 415 when contentType header is missing") {
        result2.status shouldBe 415
        result2.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      val result3 = await(doPost(uriToTest, "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml"))
      withClue("should respond with 415 when contentType header isn't JSON") {
        result3.status shouldBe 415
        result3.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }
  }
}
