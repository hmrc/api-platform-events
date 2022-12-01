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
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.{Authorization, RequestId, SessionId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

import java.time.LocalDateTime
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId
import org.scalatest.OptionValues

class ApplicationEventsServiceSpec extends AsyncHmrcSpec with Eventually with ApplicationEventTestData with OptionValues {

  val mockRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]

  val now = LocalDateTime.now()
  val nowButLastYear = now.minusYears(1)
  val year = now.getYear()
  val lastYear = nowButLastYear.getYear()

  val validAddTeamMemberModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime= LocalDateTime.now,
    actor = OldStyleActors.GatekeeperUser("iam@admin.com"),
    teamMemberEmail = LaxEmailAddress("bob@bob.com"),
    teamMemberRole = "ADMIN")

  val validProdAppNameChange: ProductionAppNameChangedEvent = ProductionAppNameChangedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime= LocalDateTime.now,
    actor = Actors.GatekeeperUser("gk@example.com"),
    oldAppName = "old app name",
    newAppName = "new app name",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  implicit val hc: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
      sessionId = Some(SessionId("dummy session id")),
      requestId = Some(RequestId("dummy request id")))

  trait Setup {
    def primeService(repoResult: Boolean, repoThrowsException: Boolean, appEvent: AbstractApplicationEvent) = {
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
    def primeRepo(events: AbstractApplicationEvent*): List[AbstractApplicationEvent] = {
      when(mockRepository.fetchEvents(*[ApplicationId])).thenReturn(Future.successful(events.toList))
      events.toList
    }

    "return everything when no queries" in new Setup {
      val appId = ApplicationId.random

      val evts = primeRepo(
        makeTeamMemberAddedEvent(Some(appId)), 
        makeTeamMemberRemovedEvent(Some(appId)),
        makeClientSecretAddedEvent(Some(appId)),
        makeClientSecretAdded(Some(appId)),
        makeClientSecretRemovedEvent(Some(appId)),
        makeClientSecretRemoved(Some(appId)),
        makeApiSubscribedEvent(Some(appId)),
        makeApiSubscribed(Some(appId)),
        makeApiUnsubscribedEvent(Some(appId)),
        makeApiUnsubscribed(Some(appId)),
        makeRedirectUrisUpdated(Some(appId))
      )

      val fetchedEvents = await(inTest.fetchEventsBy(appId, None))

      fetchedEvents shouldBe evts
    }

    "return everything for an eventType" in new Setup {
      val appId = ApplicationId.random

      val evts = primeRepo(
        makeTeamMemberAddedEvent(Some(appId)),
        makeTeamMemberAddedEvent(Some(appId))
      )

      val fetchedEvents = await(inTest.fetchEventsBy(appId, Some(EventTags.COLLABORATOR)))

      fetchedEvents should contain allOf(evts(0), evts(1))

      val fetchNoEvents = await(inTest.fetchEventsBy(appId, Some(EventTags.SUBSCRIPTION)))

      fetchNoEvents shouldBe Seq()
    }
  }

  "fetchEventQueryValues" should {
    def primeEmptyRepo(): Unit = {
      when(mockRepository.fetchEvents(*[ApplicationId])).thenReturn(Future.successful(List.empty))
    }

    def primeRepo(events: AbstractApplicationEvent*): List[AbstractApplicationEvent] = {
      when(mockRepository.fetchEvents(*[ApplicationId])).thenReturn(Future.successful(events.toList))
      events.toList
    }

    "return None when no records found for application" in new Setup {
      val appId = ApplicationId.random

      primeEmptyRepo()

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      fetchEventQueryValues shouldBe None
    }

    "return correct distinct event tags when records found for application" in new Setup {
      val appId = ApplicationId.random

      primeRepo(
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeTeamMemberAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeTeamMemberRemovedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretAddedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretAdded(Some(appId)).copy(eventDateTime = now),
        makeClientSecretRemovedEvent(Some(appId)).copy(eventDateTime = now),
        makeClientSecretRemoved(Some(appId)).copy(eventDateTime = now),
        makeApiSubscribedEvent(Some(appId)).copy(eventDateTime = now),
        makeApiSubscribed(Some(appId)).copy(eventDateTime = now),
        makeApiUnsubscribedEvent(Some(appId)).copy(eventDateTime = now),
        makeApiUnsubscribed(Some(appId)).copy(eventDateTime = now),
        makeRedirectUrisUpdated(Some(appId)).copy(eventDateTime = now),
        makeRedirectUrisUpdatedEvent(Some(appId)).copy(eventDateTime = now)
      )

      val fetchEventQueryValues = await(inTest.fetchEventQueryValues(appId))

      fetchEventQueryValues.value.eventTags should contain only(EventTags.COLLABORATOR, EventTags.CLIENT_SECRET, EventTags.SUBSCRIPTION, EventTags.REDIRECT_URIS)

      fetchEventQueryValues.value.eventTags should not contain EventTags.PPNS_CALLBACK
    }
  }
}