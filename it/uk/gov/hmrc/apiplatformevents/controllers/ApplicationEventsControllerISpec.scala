package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.{ActorType, EventId, GatekeeperUserActor}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

import java.util.UUID
import java.{util => ju}
import scala.concurrent.Future

class ApplicationEventsControllerISpec extends ServerBaseISpec  with AuditService with BeforeAndAfterEach {

  this: Suite with ServerProvider =>

  def oldRepo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]
  def repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    primeAuditService()
    await(oldRepo.collection.drop().toFuture())
  }

  val url = s"http://localhost:$port"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val eventId: UUID = EventId.random.value
  val applicationId: String = ju.UUID.randomUUID.toString
  val actorId = "123454654"
  val actorEmail = "actor@example.com"
  val actorTypeGK = ActorType.GATEKEEPER
  val actorUser = "gatekeeper"
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
         |"eventType": "PROD_APP_NAME_CHANGED",
         |"actor": { "user": "$actorUser", "actorType": "$actorTypeGK" },
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

  def checkCommonEventValues(event: ApplicationEvent): Unit = {
    event.applicationId shouldBe applicationId
    event.eventDateTime.toString shouldBe eventDateTimeString
  }

  "ApplicationEventsController" when {

    "POST /teamMemberAdded" should {
      "respond with 201 when valid json is sent" in {
        val teamMemberEmail = "bob@bob.com"
        val adminRole = "ADMIN"

        testSuccessScenario("/application-events/teamMemberAdded", validTeamMemberJsonBody(teamMemberEmail, adminRole))
        val results = await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[TeamMemberAddedEvent]

        checkCommonEventValues(event)
        event.teamMemberEmail shouldBe teamMemberEmail
        event.teamMemberRole shouldBe adminRole
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/teamMemberAdded")
      }
    }

    "POST /teamMemberRemoved" should {
      "respond with 201 when valid json is sent" in {
        val teamMemberEmail = "bob@bob.com"
        val adminRole = "ADMIN"

        testSuccessScenario("/application-events/teamMemberRemoved", validTeamMemberJsonBody(teamMemberEmail, adminRole))

        val results = await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[TeamMemberRemovedEvent]

        checkCommonEventValues(event)
        event.teamMemberEmail shouldBe teamMemberEmail
        event.teamMemberRole shouldBe adminRole
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/teamMemberRemoved")
      }
    }

    "POST /clientSecretAdded" should {
      "respond with 201 when valid json is sent" in {
        val clientSecretId = ju.UUID.randomUUID().toString

        testSuccessScenario("/application-events/clientSecretAdded", validClientSecretJsonBody(clientSecretId))


        val results = await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ClientSecretAddedEvent]

        checkCommonEventValues(event)
        event.clientSecretId shouldBe clientSecretId
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/clientSecretAdded")
      }
    }

    "POST /clientSecretRemoved" should {
      "respond with 201 when valid json is sent" in {
        val clientSecretId = ju.UUID.randomUUID().toString

        testSuccessScenario("/application-events/clientSecretRemoved", validClientSecretJsonBody(clientSecretId))

        val results = await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ClientSecretRemovedEvent]

        checkCommonEventValues(event)
        event.clientSecretId shouldBe clientSecretId
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/clientSecretRemoved")
      }
    }

    "POST /redirectUrisUpdated" should {
      "respond with 201 when valid json is sent" in {
        val oldRedirectUri = "oldrdu"
        val newRedirectUri = "newrdu"

        testSuccessScenario("/application-events/redirectUrisUpdated", validRedirectUrisUpdatedJsonBody(oldRedirectUri, newRedirectUri))

        val results = await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[RedirectUrisUpdatedEvent]

        checkCommonEventValues(event)
        event.oldRedirectUris shouldBe oldRedirectUri
        event.newRedirectUris shouldBe newRedirectUri
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/redirectUrisUpdated")
      }
    }

    "POST /apiSubscribed" should {
      "respond with 201 when valid json is sent" in {
        val apiContext = "apicontext"
        val apiVersion = "1.0"

        testSuccessScenario("/application-events/apiSubscribed", validApiSubscriptionJsonBody(apiContext, apiVersion))
        val results =await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApiSubscribedEvent]

        checkCommonEventValues(event)
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/apiSubscribed")
      }
    }

    "POST /apiUnsubscribed" should {
      "respond with 201 when valid json is sent" in {
        val apiContext = "apicontext"
        val apiVersion = "1.0"

        testSuccessScenario("/application-events/apiUnsubscribed", validApiSubscriptionJsonBody(apiContext, apiVersion))
        val results = await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApiUnsubscribedEvent]

        checkCommonEventValues(event)
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/apiUnsubscribed")
      }
    }

    "POST /ppnsCallbackUriUpdated" should {
      "respond with 201 when valid json is sent" in {
        val boxId = ju.UUID.randomUUID().toString
        val boxName = "some##box##name"
        val oldCallbackUrl = "https://foo.bar/baz"
        val newCallbackUrl = "https://foo.bar/bazbazbaz"

        testSuccessScenario("/application-events/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody(boxId, boxName, oldCallbackUrl, newCallbackUrl))

        val results =await(oldRepo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[PpnsCallBackUriUpdatedEvent]

        checkCommonEventValues(event)
        event.boxId shouldBe boxId
        event.oldCallbackUrl shouldBe oldCallbackUrl
        event.newCallbackUrl shouldBe newCallbackUrl
        event.boxName shouldBe boxName
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/ppnsCallbackUriUpdated")
      }

    }

    "POST /application-event" should {
      "respond with 201 when valid json is sent" in {
        val oldAppName = "old name"
        val newAppName = "new name"
        val requestingAdminName = "mrs admin"

        testSuccessScenario("/application-event", validProductionAppNameChangedJsonBody(oldAppName, newAppName, requestingAdminName))

        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ProductionAppNameChangedEvent]

        checkCommonEventValues(event)
        event.oldAppName shouldBe oldAppName
        event.newAppName shouldBe newAppName
        event.requestingAdminName shouldBe requestingAdminName

        event.actor match {
          case GatekeeperUserActor(name) => name shouldBe actorUser
          case _ => fail("expected GatekeeperUserActor")
        }

      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-event")
      }

    }

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
