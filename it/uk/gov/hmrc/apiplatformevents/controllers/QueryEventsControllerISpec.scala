package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.{ActorType, EventId}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

import java.util.UUID
import java.{util => ju}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatformevents.models.common.EventType

class QueryEventsControllerISpec extends ServerBaseISpec  with AuditService with BeforeAndAfterEach with ApplicationEventTestData with JsonRequestFormatters {

  this: Suite with ServerProvider =>

  def repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    primeAuditService()
    await(repo.collection.drop().toFuture())
  }

  def doGet(path: String): Future[WSResponse] = {
    wsClient
      .url(s"$url$path")
      .get()
  }

  val url = s"http://localhost:$port"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val eventId: UUID = EventId.random.value
  val applicationId = UUID.randomUUID().toString
  val submissionId: String = ju.UUID.randomUUID.toString
  val actorId = "123454654"
  val actorEmail = "actor@example.com"
  val actorTypeGK = ActorType.GATEKEEPER
  val actorTypeCollab = ActorType.COLLABORATOR
  val actorUser = "gatekeeper"
  val eventDateTimeString = "2014-01-01T13:13:34.441"
  val appId = UUID.randomUUID().toString()
  
  private def primeMongo(events: ApplicationEvent*): Array[ApplicationEvent] = {
    await(Future.sequence(events.toList.map(repo.createEntity(_))))
    events.toArray.sorted(QueryEventsController.orderEvents)
  }
  
  "QueryEventsController" when {
    import cats.implicits._

    "validateEventType" should {
      "pass for a genuine event type" in {
        QueryEventsController.validateEventType("TEAM_MEMBER_ADDED") shouldBe (EventType.TEAM_MEMBER_ADDED).validNec
      }
      "fail for a fake event type" in {
        QueryEventsController.validateEventType("BLOBBY_BLOBBY_BLOBBY") shouldBe "EventType BLOBBY_BLOBBY_BLOBBY is invalid".invalidNec
      }
    }

    "validateYear" should {
      "pass for a genuine year" in {
        QueryEventsController.validateYear("1990") shouldBe 1990.validNec
      }
      "fail for a fake year" in {
        QueryEventsController.validateYear("BLOBBY_BLOBBY_BLOBBY") shouldBe "BLOBBY_BLOBBY_BLOBBY is not a valid year".invalidNec
      }      
    }

    "GET /application-events/:id" should {

      "return 404 when no records exists" in {
        val result = await(doGet(s"/application-event/$appId"))
        result.status shouldBe 200
        result.body shouldBe """{"events":[]}"""        
      }
      
      "return all relevant events" in {
        val evts = primeMongo(
          makeTeamMemberAddedEvent(Some(appId)),
          makeApiSubscribedEvent(Some(appId))
        )
        
        val result = await(doGet(s"/application-event/$appId"))
        result.status shouldBe 200
        val expectedText = Json.asciiStringify(Json.toJson(QueryEventsController.QueryResponse(evts.toSeq.sorted(QueryEventsController.orderEvents))))
        result.body shouldBe expectedText
      }
    }
  }
}
