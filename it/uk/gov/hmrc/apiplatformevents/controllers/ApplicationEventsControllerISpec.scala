package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import java.{util => ju}
import uk.gov.hmrc.apiplatformevents.models.common.ApplicationEvent
import uk.gov.hmrc.apiplatformevents.models.TeamMemberRemovedEvent
import uk.gov.hmrc.apiplatformevents.models.ClientSecretAddedEvent
import uk.gov.hmrc.apiplatformevents.models.ClientSecretRemovedEvent
import uk.gov.hmrc.apiplatformevents.models.RedirectUrisUpdatedEvent
import uk.gov.hmrc.apiplatformevents.models.ApiSubscribedEvent
import uk.gov.hmrc.apiplatformevents.models.ApiUnsubscribedEvent
import uk.gov.hmrc.apiplatformevents.models.PpnsCallBackUriUpdatedEvent


class ApplicationEventsControllerISpec extends ServerBaseISpec with MongoSpecSupport with AuditService with  BeforeAndAfterEach {

  this: Suite with ServerProvider =>

  override def beforeEach(): Unit = {
    super.beforeEach()
      primeAuditService()
    dropMongoDb()(global)
  }

  def dropMongoDb()(implicit ec: ExecutionContext): Unit = {
   await(mongo().drop())
}


  def repo: ApplicationEventsRepository =
    app.injector.instanceOf[ApplicationEventsRepository]


  val url = s"http://localhost:$port/application-events"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val applicationId = ju.UUID.randomUUID.toString
  val teamMemberEmail =  "bob@bob.com"
  val adminRole = "ADMIN"
  val actorId = "123454654"
  val actorTypeGK = "GATEKEEPER"
  val eventDateTimeString = "2014-01-01T13:13:34.441Z"
  val clientSecretId = "abababab"
  val oldRedirectUri = "oldrdu"
  val newRedirectUri = "newrdu"
  val apiContext = "apicontext"
  val apiVersion = "1.0"
  val boxId = "someBoxId"
  val oldCallbackUrl = "oldUrl"
  val newCallbackUrl = "newUrl"

  val validTeamMemberJsonBody: String =
    raw"""{"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"teamMemberEmail": "$teamMemberEmail",
         |"teamMemberRole": "$adminRole"}""".stripMargin

  val validClientSecretJsonBody: String =
  raw"""{"applicationId": "$applicationId",
        |"eventDateTime": "$eventDateTimeString",
        |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
        |"clientSecretId": "$clientSecretId"}""".stripMargin

  val validRedirectUrisUpdatedJsonBody: String =
    raw"""{"applicationId": "$applicationId",
           |"eventDateTime": "$eventDateTimeString",
           |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
           |"oldRedirectUris": "$oldRedirectUri",
           |"newRedirectUris": "$newRedirectUri"}""".stripMargin

  val validApiSubscriptionJsonBody: String =
    raw"""{"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"context": "$apiContext",
         |"version": "$apiVersion"}""".stripMargin

  val validPpnsCallBackUpdatedJsonBody: String =
    raw"""{"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"boxId": "$boxId",
         |"context": "$apiContext",
         |"version": "$apiVersion",
         |"oldCallbackUrl": "$oldCallbackUrl",
         |"newCallbackUrl": "$newCallbackUrl"}""".stripMargin

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

 def checkCommonEventValues[A <: ApplicationEvent](event: A){
            event.applicationId shouldBe applicationId
            event.eventDateTime.toString() shouldBe eventDateTimeString
            event.actor.id shouldBe actorId
            event.actor.actorType.toString shouldBe actorTypeGK
 }

  "ApplicationEventsController" when {

    "POST /teamMemberAdded" should {
      "respond with 201 when valid json is sent" in {
        testSuccessScenario("/teamMemberAdded", validTeamMemberJsonBody)
        val results = await(repo.findAll()(global))
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
         testSuccessScenario("/teamMemberRemoved", validTeamMemberJsonBody)

        val results = await(repo.findAll()(global))
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
         testSuccessScenario("/clientSecretAdded", validClientSecretJsonBody)


        val results = await(repo.findAll()(global))
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
        testSuccessScenario("/clientSecretRemoved", validClientSecretJsonBody)

        val results = await(repo.findAll()(global))
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
        testSuccessScenario("/redirectUrisUpdated", validRedirectUrisUpdatedJsonBody)

        val results = await(repo.findAll()(global))
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
        testSuccessScenario("/apiSubscribed", validApiSubscriptionJsonBody)
        val results = await(repo.findAll()(global))
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
        testSuccessScenario("/apiUnsubscribed", validApiSubscriptionJsonBody)
       val results = await(repo.findAll()(global))
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
        testSuccessScenario("/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody)

          val results = await(repo.findAll()(global))
        results.size shouldBe 1
        val event = results.head.asInstanceOf[PpnsCallBackUriUpdatedEvent]

        checkCommonEventValues(event)
        event.boxId shouldBe boxId
        event.oldCallbackUrl shouldBe oldCallbackUrl
        event.newCallbackUrl shouldBe newCallbackUrl
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
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
