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

import java.util.UUID

import org.mongodb.scala.MongoException
import org.scalatest.concurrent.Eventually
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.{OldActor, OldActorType, EventId}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.{Authorization, RequestId, SessionId}

import scala.concurrent.Future
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

import java.time.LocalDateTime

class ApplicationEventsServiceSpec extends AsyncHmrcSpec with Eventually {

  val mockRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]

  val validAddTeamMemberModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = EventId.random,
    applicationId = UUID.randomUUID().toString,
    eventDateTime= LocalDateTime.now,
    actor = OldActor("iam@admin.com", OldActorType.GATEKEEPER),
    teamMemberEmail = "bob@bob.com",
    teamMemberRole = "ADMIN")


  implicit val hc: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
      sessionId = Some(SessionId("dummy session id")),
      requestId = Some(RequestId("dummy request id")))

  trait Setup {
    def primeService(repoResult: Boolean, repoThrowsException: Boolean) = {
      if (repoThrowsException) {
        when(mockRepository.createEntity(eqTo(validAddTeamMemberModel)))
          .thenReturn(Future.failed(new MongoException("some mongo error")))
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

      val exception: MongoException = intercept[MongoException] {
        await(inTest.captureEvent(validAddTeamMemberModel))
      }

      exception.getMessage shouldBe "some mongo error"
    }
  }
}
