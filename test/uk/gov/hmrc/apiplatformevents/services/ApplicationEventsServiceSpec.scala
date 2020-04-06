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
import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import reactivemongo.core.errors.ReactiveMongoException
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationEventsServiceSpec
    extends UnitSpec
    with MockitoSugar
    with Eventually {


  val mockRepository = mock[ApplicationEventsRepository]

  val validModel = TeamMemberAddedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.GATEKEEPER),
    teamMemberEmail = "bob@bob.com",
    teamMemberRole = "ADMIN")

  implicit val hc =
    HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
      sessionId = Some(SessionId("dummy session id")),
      requestId = Some(RequestId("dummy request id")))

  "ApplicationEventsService" should {

    "send an TeamMemberAdded event to the repository and return true when saved" in {
        testService(true, false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(false, false) shouldBe false
    }


    "handle error" in {
        testService(false, true) shouldBe false
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[TeamMemberAddedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[TeamMemberAddedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureEvent(validModel))
    }
  }
}
