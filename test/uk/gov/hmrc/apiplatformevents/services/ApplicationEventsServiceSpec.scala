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

import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationEventsServiceSpec
    extends UnitSpec
    with MockitoSugar
    with Eventually {

  "ApplicationEventsService" should {

    "send an TeamMemberAdded event with the correct fields" in {
      val mockRepository = mock[ApplicationEventsRepository]
      when(mockRepository.createEntity(any[TeamMemberAddedEvent])(any()))
        .thenReturn(Future.successful(true))

      val service = new ApplicationEventsService(mockRepository)

      implicit val hc =
        HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
                      sessionId = Some(SessionId("dummy session id")),
                      requestId = Some(RequestId("dummy request id")))

      val model = TeamMemberAddedEvent(applicationId = UUID.randomUUID().toString,
        1234547657L,
        "bob@bob.com",
        "ADMIN")

      await(service.captureEvent(model)) shouldBe true

    }
  }
}
