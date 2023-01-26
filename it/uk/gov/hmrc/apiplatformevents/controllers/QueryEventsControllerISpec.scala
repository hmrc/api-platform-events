package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import play.api.libs.json.Json

import java.time.Instant
import java.time.temporal.ChronoUnit
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.AbstractApplicationEvent
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventId
import java.util.UUID
import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress

class QueryEventsControllerISpec extends ServerBaseISpec with AuditService with BeforeAndAfterEach with ApplicationEventTestData {

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

  val eventId              = EventId.random
  val applicationId        = ApplicationId.random
  val submissionId: String = UUID.randomUUID.toString
  val actorId              = "123454654"
  val actorEmail           = LaxEmailAddress("actor@example.com")
  val actorUser            = "gatekeeper"
  val eventDateTimeString  = "2014-01-01T13:13:34.441"
  val appId                = ApplicationId.random

  private def primeMongo(events: AbstractApplicationEvent*): List[AbstractApplicationEvent] = {
    await(Future.sequence(events.toList.map(repo.createEntity(_))))
    events.toList.sorted(AbstractApplicationEvent.orderEvents)
  }

  "QueryEventsController" when {

    "GET /application-events/:id" should {

      "return 404 when no records exists" in {
        val result = await(doGet(s"/application-event/${appId.value.toString}"))
        result.status shouldBe 404
      }

      "return all relevant events" in {
        val event1 = makeTeamMemberAddedEvent(Some(appId))
        val event2 = makeApiSubscribedEvent(Some(appId))
        val evts   = primeMongo(
          event1.copy(eventDateTime = Instant.now.minus(2, ChronoUnit.DAYS)),
          event2.copy(eventDateTime = Instant.now.minus(1, ChronoUnit.DAYS))
        )

        val result       = await(doGet(s"/application-event/${appId.value.toString}"))
        result.status shouldBe 200
        val expectedText = Json.asciiStringify(Json.toJson(QueryEventsController.QueryResponse(evts.sorted(AbstractApplicationEvent.orderEvents))))
        result.body shouldBe expectedText
      }
    }
  }
}
