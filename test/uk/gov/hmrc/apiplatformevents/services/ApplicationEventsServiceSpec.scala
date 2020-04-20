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
import uk.gov.hmrc.apiplatformevents.models.{ApiSubscribedEvent, ApiUnsubscribedEvent, ClientSecretAddedEvent, ClientSecretRemovedEvent, RedirectUrisUpdatedEvent, TeamMemberAddedEvent, TeamMemberRemovedEvent}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, RequestId, SessionId}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import reactivemongo.core.errors.{GenericDriverException, ReactiveMongoException}
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationEventsServiceSpec
    extends UnitSpec
    with MockitoSugar
    with Eventually {

  val mockRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]

  val validAddTeamMemberModel: TeamMemberAddedEvent = TeamMemberAddedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.GATEKEEPER),
    teamMemberEmail = "bob@bob.com",
    teamMemberRole = "ADMIN")

  val validRemoveTeamMemberModel: TeamMemberRemovedEvent = TeamMemberRemovedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.COLLABORATOR),
    teamMemberEmail = "bob@bob.com",
    teamMemberRole = "ADMIN")

  val validAddClientSecretModel: ClientSecretAddedEvent = ClientSecretAddedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.COLLABORATOR),
    clientSecretId = "abababab")

  val validRemoveClientSecretModel: ClientSecretRemovedEvent = ClientSecretRemovedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.COLLABORATOR),
    clientSecretId = "abababab")

  val validRedirectUrisUpdatedModel: RedirectUrisUpdatedEvent = RedirectUrisUpdatedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.COLLABORATOR),
    oldRedirectUris = "oldrdu",
    newRedirectUris = "newrdu")

  val validApiSubscribedModel: ApiSubscribedEvent = ApiSubscribedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.COLLABORATOR),
    context = "apicontext",
    version = "1.0")

  val validApiUnsubscribedModel: ApiUnsubscribedEvent = ApiUnsubscribedEvent(applicationId = UUID.randomUUID().toString,
    DateTime.now,
    actor = Actor("iam@admin.com", ActorType.COLLABORATOR),
    context = "apicontext",
    version = "1.0")

  implicit val hc: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization("dummy bearer token")),
      sessionId = Some(SessionId("dummy session id")),
      requestId = Some(RequestId("dummy request id")))

  "TeamMemberServiceAdded" should {

    "send a TeamMemberAdded event to the repository and return true when saved" in {
        testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
     val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
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
      await(service.captureTeamMemberAddedEvent(validAddTeamMemberModel))
    }
  }

  "TeamMemberServiceRemoved" should {

    "send a TeamMemberRemoved event to the repository and return true when saved" in {
      testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
      val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[TeamMemberRemovedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[TeamMemberRemovedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureTeamMemberRemovedEvent(validRemoveTeamMemberModel))
    }
  }

  "ClientSecretServiceAdded" should {

    "send a ClientSecretAdded event to the repository and return true when saved" in {
      testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
      val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[ClientSecretAddedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[ClientSecretAddedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureClientSecretAddedEvent(validAddClientSecretModel))
    }
  }

  "ClientSecretServiceRemoved" should {

    "send a ClientSecretRemoved event to the repository and return true when saved" in {
      testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
      val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[ClientSecretRemovedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[ClientSecretRemovedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureClientSecretRemovedEvent(validRemoveClientSecretModel))
    }
  }

  "RedirectUrisUpdatedService" should {

    "send a RedirectUrisUpdated event to the repository and return true when saved" in {
      testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
      val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[RedirectUrisUpdatedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[RedirectUrisUpdatedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureRedirectUrisUpdatedEvent(validRedirectUrisUpdatedModel))
    }
  }

  "ApiSubscribedService" should {

    "send a ApiSubscribed event to the repository and return true when saved" in {
      testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
      val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[ApiSubscribedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[ApiSubscribedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureApiSubscribedEvent(validApiSubscribedModel))
    }
  }

  "ApiUnsubscribedService" should {

    "send a ApiUnsubscribed event to the repository and return true when saved" in {
      testService(repoResult = true, repoThrowsException = false) shouldBe true
    }


    "fail and return false when repository capture event fails" in {
      testService(repoResult = false, repoThrowsException = false) shouldBe false
    }


    "handle error" in {
      val exception =  intercept[GenericDriverException] {
        testService(repoResult = false, repoThrowsException = true)
      }

      exception.message shouldBe "some mongo error"
    }

    def testService(repoResult: Boolean, repoThrowsException: Boolean): Boolean = {
      if(repoThrowsException){
        when(mockRepository.createEntity(any[ApiUnsubscribedEvent])(any()))
          .thenReturn(Future.failed(ReactiveMongoException("some mongo error")))
      }else {
        when(mockRepository.createEntity(any[ApiUnsubscribedEvent])(any()))
          .thenReturn(Future.successful(repoResult))
      }

      val service = new ApplicationEventsService(mockRepository)
      await(service.captureApiUnsubscribedEvent(validApiUnsubscribedModel))
    }
  }

}
