/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformevents.services

import org.mongodb.scala.MongoException
import org.scalatest.concurrent.Eventually
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.{ActorType, EventId, GatekeeperUserActor, OldActor}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.{Authorization, RequestId, SessionId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

import java.time.LocalDateTime
import java.util.UUID
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import uk.gov.hmrc.apiplatformevents.models.common.EventType
import uk.gov.hmrc.apiplatformevents.models.common.CollaboratorActor

class ApplicationEventsServiceSpec extends AsyncHmrcSpec with Eventually with ApplicationEventTestData {

  val mockRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]

  val now = LocalDateTime.now()
  val nowButLastYear = now.minusYears(1)
  val year = now.getYear()
  val lastYear = nowButLastYear.getYear()

  val validAddTeamMemberModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = EventId.random,
    applicationId = UUID.randomUUID().toString,
    eventDateTime= LocalDateTime.now,
    actor = OldActor("iam@admin.com", ActorType.GATEKEEPER),
    teamMemberEmail = "bob@bob.com",
    teamMemberRole = "ADMIN")

  val validProdAppNameChange: ProductionAppNameChangedEvent = ProductionAppNameChangedEvent(
    id = EventId.random,
    applicationId = UUID.randomUUID().toString,
    eventDateTime= LocalDateTime.now,
    actor = GatekeeperUserActor("gk@example.com"),
    oldAppName = "old app name",
    newAppName = "new app name",
    requestingAdminEmail = "admin@example.com")

  implicit val hc: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
      sessionId = Some(SessionId("dummy session id")),
      requestId = Some(RequestId("dummy request id")))

  trait Setup {
    def primeService(repoResult: Boolean, repoThrowsException: Boolean, appEvent: ApplicationEvent) = {
      if (repoThrowsException) {
        when(mockRepository.createEntity(eqTo(appEvent)))
          .thenReturn(Future.failed(new MongoException("some mongo error")))
      } else {
        when(mockRepository.createEntity(eqTo(appEvent)))
          .thenReturn(Future.successful(repoResult))
      }
    }

    val inTest = new ApplicationEventsService(mockRepository)
  }

  "Capture old event" should {
    "send an event to the repository and return true when saved" in new Setup {
      primeService(repoResult = true, repoThrowsException = false, validAddTeamMemberModel)
      await(inTest.captureEvent(validAddTeamMemberModel)) shouldBe true
    }

    "fail and return false when repository capture event fails" in new Setup {
      primeService(repoResult = false, repoThrowsException = false, validAddTeamMemberModel)
      await(inTest.captureEvent(validAddTeamMemberModel)) shouldBe false
    }

    "handle error" in new Setup {
      primeService(repoResult = false, repoThrowsException = true, validAddTeamMemberModel)

      val exception: MongoException = intercept[MongoException] {
        await(inTest.captureEvent(validAddTeamMemberModel))
      }

      exception.getMessage shouldBe "some mongo error"
    }
  }

  "Capture event" should {
    "send an event to the repository and return true when saved" in new Setup {
      primeService(repoResult = true, repoThrowsException = false, validProdAppNameChange)
      await(inTest.captureEvent(validProdAppNameChange)) shouldBe true
    }

    "fail and return false when repository capture event fails" in new Setup {
      primeService(repoResult = false, repoThrowsException = false, validProdAppNameChange)
      await(inTest.captureEvent(validProdAppNameChange)) shouldBe false
    }

    "handle error" in new Setup {
      primeService(repoResult = false, repoThrowsException = true, validProdAppNameChange)

      val exception: MongoException = intercept[MongoException] {
        await(inTest.captureEvent(validProdAppNameChange))
      }

      exception.getMessage shouldBe "some mongo error"
    }
  }

  "fetch events by" should {
    def primeRepo(events: ApplicationEvent*): Seq[ApplicationEvent] = {
      when(mockRepository.fetchEventsBy(*,eqTo(None))).thenReturn(Future.successful(events.toSeq))
      events.toSeq
    }

    def primeRepoFor(eventType: EventType)(events: ApplicationEvent*): Seq[ApplicationEvent] = {
      when(mockRepository.fetchEventsBy(*,eqTo(Some(eventType)))).thenReturn(Future.successful(events.toSeq))
      events.toSeq
    }

    "return everything when no queries" in new Setup {
      val appId = UUID.randomUUID().toString()

      val evts = primeRepo(
        makeTeamMemberAddedEvent(Some(appId)), 
        makeTeamMemberRemovedEvent(Some(appId)),
        makeClientSecretAddedEvent(Some(appId))
      )

      val fetchedEvents = await(inTest.fetchEventsBy(appId, None, None, None))

      fetchedEvents should contain allOf(evts(0), evts(1), evts(2))
    }

    "return based on year" in new Setup {
      val appId = UUID.randomUUID().toString()

      val evts = primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberRemovedEvent(Some(appId)).copy(eventDateTime = now.withMinute(10)),
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = nowButLastYear)
      )

      val fetchedEvents1900 = await(inTest.fetchEventsBy(appId, Some(1900), None, None))
      fetchedEvents1900 shouldBe Seq.empty

      val fetchedEventsLastYear = await(inTest.fetchEventsBy(appId, Some(lastYear), None, None))
      fetchedEventsLastYear should contain only (evts(2))

      val fetchedEventsNow = await(inTest.fetchEventsBy(appId, Some(year), None, None))
      fetchedEventsNow should contain allOf(evts(0), evts(1))
    }

    "return everything for an eventType" in new Setup {
      val appId = UUID.randomUUID().toString()

      val evts = primeRepoFor(
        EventType.TEAM_MEMBER_ADDED
      )(
        makeTeamMemberAddedEvent(Some(appId)), 
        makeTeamMemberAddedEvent(Some(appId))
      )

      val fetchedEvents = await(inTest.fetchEventsBy(appId, None, Some(EventType.TEAM_MEMBER_ADDED), None))

      fetchedEvents should contain allOf(evts(0), evts(1))
    }

    "return events for an actor matching OldActor" in new Setup {
     val appId = UUID.randomUUID().toString()

      val evts = primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(actor = OldActor("bob", ActorType.COLLABORATOR)), 
        makeClientSecretAddedEvent(Some(appId)).copy(actor = OldActor("bob", ActorType.COLLABORATOR)), 
        makeTeamMemberRemovedEvent(Some(appId))
      )

      val fetchedEvents = await(inTest.fetchEventsBy(appId, None, None, Some("bob")))

      fetchedEvents should contain allOf(evts(0), evts(1))
    }

    "return events for an actor matching Actor" in new Setup {
     val appId = UUID.randomUUID().toString()

      val evts = primeRepo(
        makeProductionAppNameChangedEvent(Some(appId)).copy(actor = CollaboratorActor("bob")), 
        makeResponsibleIndividualChanged(Some(appId)).copy(actor = GatekeeperUserActor("bob")), 
        makeRedirectUrisUpdatedEvent(Some(appId))
      )

      val fetchedEvents = await(inTest.fetchEventsBy(appId, None, None, Some("bob")))

      fetchedEvents should contain allOf(evts(0), evts(1))
    }
  }

  "fetchfetchEventQueryValues" should {
    def primeEmptyRepo(): Unit = {
      when(mockRepository.fetchEvents(*)).thenReturn(Future.successful(Seq.empty))
    }

    def primeRepo(events: ApplicationEvent*): Seq[ApplicationEvent] = {
      when(mockRepository.fetchEvents(*)).thenReturn(Future.successful(events.toSeq))
      events.toSeq
    }

    "return None when no records found for application" in new Setup {
      val appId = UUID.randomUUID().toString()

      primeEmptyRepo()

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      fetchEventQueryValues shouldBe None
    }

    "return correct first year value when records found for application" in new Setup {
      val appId = UUID.randomUUID().toString()

      primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = nowButLastYear)
      )

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      inside(fetchEventQueryValues.value) { case QueryableValues(firstYear, _,_,_) =>
        firstYear shouldBe lastYear
      }
    }

    "return correct last year value when records found for application" in new Setup {
      val appId = UUID.randomUUID().toString()

      primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = nowButLastYear)
      )

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      inside(fetchEventQueryValues.value) { case QueryableValues(_, lastYear,_,_) =>
        lastYear shouldBe year
      }
    }

    "return correct distinct event types when records found for application" in new Setup {
      val appId = UUID.randomUUID().toString()

      primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now), 
        makeTeamMemberRemovedEvent(Some(appId)).copy(eventDateTime = now), 
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = now)
      )

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      inside(fetchEventQueryValues.value) { case QueryableValues(_, _, eventTypes, _) =>
        eventTypes should contain allOf (EventType.TEAM_MEMBER_ADDED, EventType.TEAM_MEMBER_REMOVED, EventType.CLIENT_SECRET_ADDED)
        eventTypes should not contain EventType.CLIENT_SECRET_REMOVED
      }
    }

    "return correct actors when records found" in new Setup {
      val appId = UUID.randomUUID().toString()

      primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(actor = OldActor("alice", ActorType.COLLABORATOR)), 
        makeTeamMemberAddedEvent(Some(appId)).copy(actor = OldActor("bob", ActorType.COLLABORATOR)), 
        makeTeamMemberAddedEvent(Some(appId)).copy(actor = OldActor("charlie", ActorType.COLLABORATOR)), 
        makeClientSecretAddedEvent(Some(appId)).copy(actor = OldActor("alice", ActorType.COLLABORATOR)), 
        makeTeamMemberAddedEvent(Some(appId)).copy(actor = OldActor("bob", ActorType.COLLABORATOR)), 
        makeClientSecretAddedEvent(Some(appId)).copy(actor = OldActor("charlie", ActorType.COLLABORATOR)), 
        makeProductionAppNameChangedEvent(Some(appId)).copy(actor = CollaboratorActor("charlie")), 
        makeResponsibleIndividualChanged(Some(appId)).copy(actor = GatekeeperUserActor("alice")), 
        makeProductionAppNameChangedEvent(Some(appId)).copy(actor = CollaboratorActor("dylan")), 
        makeResponsibleIndividualChanged(Some(appId)).copy(actor = GatekeeperUserActor("ellie"))
      )

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      inside(fetchEventQueryValues.value) { case QueryableValues(_, _, _, actors) =>
        actors should contain allOf ("alice","bob","charlie", "dylan", "ellie")
      }
    }
  }
}