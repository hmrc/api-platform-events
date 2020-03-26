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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apiplatformevents.models.ApiPlatformEventsModel
import uk.gov.hmrc.apiplatformevents.repository.{
  ApiPlatformEventsDBModel,
  ApiPlatformEventsRepository
}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ApiPlatformEventsServiceSpec
    extends UnitSpec
    with MockitoSugar
    with Eventually {

  "auditService" should {

    "send an ApiPlatformEvents event with the correct fields" in {
      val mockRepository = mock[ApiPlatformEventsRepository]
      val service = new ApiPlatformEventService(mockRepository)
      when(
        mockRepository.createEntity(any[ApiPlatformEventsDBModel])(
          any[ExecutionContext])).thenReturn(Future.successful(()))

      implicit val hc =
        HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
                      sessionId = Some(SessionId("dummy session id")),
                      requestId = Some(RequestId("dummy request id")))

      val model = ApiPlatformEventsModel(parameter1 = "John Smith",
                                         parameter2 = None,
                                         telephoneNumber = Some("12313"),
                                         emailAddress =
                                           Some("john.smith@email.com"))
      val captor = ArgumentCaptor.forClass(classOf[ApiPlatformEventsDBModel])
      await(service.captureEvent(model)) shouldBe ()

      verify(mockRepository).createEntity(captor.capture())(any())
      val sentEvent = captor.getValue.asInstanceOf[ApiPlatformEventsDBModel]

      sentEvent.parameter1 shouldBe model.parameter1
      sentEvent.parameter2 shouldBe model.parameter2
      sentEvent.telephoneNumber shouldBe model.telephoneNumber
      sentEvent.emailAddress shouldBe model.emailAddress

    }
  }
}
