/*
 * Copyright 2020 HM Revenue & Customs
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

import java.util.UUID

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import reactivemongo.core.errors.{GenericDriverException, ReactiveMongoException}
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType, EventId}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationEventsServiceSpec
  extends UnitSpec
    with MockitoSugar
    with Eventually {

  val mockRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]

  val validAddTeamMemberModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = Some(EventId.random),
    applicationId = UUID.randomUUID().toString,
    eventDateTime= DateTime.now,
    actor = Actor("iam@admin.com", ActorType.GATEKEEPER),
    teamMemberEmail = "bob@bob.com",
    teamMemberRole = "ADMIN")


  implicit val hc: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
      sessionId = Some(SessionId("dummy session id")),
      requestId = Some(RequestId("dummy request id")))

  trait Setup {
    def primeService(repoResult: Boolean, repoThrowsException: Boolean): OngoingStubbing[Future[Boolean]] = {
      if (repoThrowsException) {
        when(mockRepository.createEntity(eqTo(validAddTeamMemberModel)))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      } else {
        when(mockRepository.createEntity(eqTo(validAddTeamMemberModel)))
          .thenReturn(Future.successful(repoResult))
      }
    }

    val inTest = new ApplicationEventsService(mockRepository)

  }

  "Capture event" should {

    "send an event to the repository and return true when saved" in new Setup {
      primeService(repoResult = true, repoThrowsException = false)
      await(inTest.captureEvent(validAddTeamMemberModel)) shouldBe true
    }


    "fail and return false when repository capture event fails" in new Setup {
      primeService(repoResult = false, repoThrowsException = false)
      await(inTest.captureEvent(validAddTeamMemberModel)) shouldBe false
    }


    "handle error" in new Setup {
      primeService(repoResult = false, repoThrowsException = true)

      val exception: GenericDriverException = intercept[GenericDriverException] {
        await(inTest.captureEvent(validAddTeamMemberModel))
      }

      exception.message shouldBe "some mongo error"
    }
  }


}
