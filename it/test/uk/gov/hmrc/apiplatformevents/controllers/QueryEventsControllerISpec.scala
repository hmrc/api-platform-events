/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apiplatformevents.controllers

import java.time.ZoneOffset
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider

import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{ApplicationId, LaxEmailAddress}
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.{ApplicationEvent, EventId}

import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import uk.gov.hmrc.apiplatformevents.models.DisplayEvent
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

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
  val inputInstantString   = "2014-01-01T13:13:34.441"
  val appId                = ApplicationId.random
  val otherAppId           = ApplicationId.random

  private def primeMongo(events: ApplicationEvent*): List[ApplicationEvent] = {
    await(Future.sequence(events.toList.map(repo.createEntity(_))))
    events.toList.sorted(ApplicationEvent.orderEvents)
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
        val event3 = makeApiSubscribedEvent(Some(otherAppId))
        val evts   = primeMongo(
          event1.copy(eventDateTime = nowMillis().atOffset(ZoneOffset.UTC).minusDays(2).toInstant),
          event2.copy(eventDateTime = nowMillis().atOffset(ZoneOffset.UTC).minusDays(1).toInstant),
          event3.copy(eventDateTime = nowMillis().atOffset(ZoneOffset.UTC).minusDays(0).toInstant)
        )

        val result       = await(doGet(s"/application-event/${appId.value.toString}"))
        result.status shouldBe 200
        val expectedText = Json.asciiStringify(Json.toJson(QueryEventsController.QueryResponse(evts.drop(1).sorted(ApplicationEvent.orderEvents).map(DisplayEvent(_)))))
        result.body shouldBe expectedText
      }

      "return all relevant events with eventTag" in {
        val event1a = makeTeamMemberAddedEvent(Some(appId))
        val event1b = makeTeamMemberAddedEvent(Some(appId))
        val event1c = makeTeamMemberAddedEvent(Some(appId))
        val event2a = makeApiSubscribedEvent(Some(appId)).copy(eventDateTime = nowMillis().atOffset(ZoneOffset.UTC).minusDays(2).toInstant)
        val event2b = makeApiSubscribedEvent(Some(appId)).copy(eventDateTime = nowMillis().atOffset(ZoneOffset.UTC).minusDays(1).toInstant)

        primeMongo(
          event1a,
          event1b,
          event1c,
          event2a,
          event2b
        )

        val expectedEvts = List(event2a, event2b)

        val result       = await(doGet(s"/application-event/${appId.value.toString}?eventTag=SUBSCRIPTION"))
        result.status shouldBe 200
        val expectedText = Json.asciiStringify(Json.toJson(QueryEventsController.QueryResponse(expectedEvts.sorted(ApplicationEvent.orderEvents).map(DisplayEvent(_)))))
        result.body shouldBe expectedText

      }
    }
  }
}
