/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformevents.scheduler.jobs

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration.{Duration, FiniteDuration}

import org.mockito.ArgumentMatchersSugar
import org.mockito.captor.ArgCaptor
import org.mockito.scalatest.MockitoSugar
import org.mongodb.scala.MongoException
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

import play.api.Configuration
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.apiplatform.modules.applications.core.domain.models.Collaborators
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{Actors, LaxEmailAddress, _}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.models.{ApplicationResponse, Notification, NotificationStatus}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.apiplatformevents.scheduler.ScheduleStatus
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig

class SendEventNotificationsServiceSpec
    extends PlaySpec
    with MockitoSugar
    with ArgumentMatchersSugar
    with FutureAwaits
    with DefaultAwaitTimeout
    with LogCapturing
    with BeforeAndAfterEach {

  val finiteDuration: FiniteDuration = Duration(4, TimeUnit.MINUTES)

  class Setup extends FixedClock {
    val mockAppConfig: AppConfig                                       = mock[AppConfig]
    val mockConfiguration                                              = mock[Configuration]
    val mockLockRepository: MongoLockRepository                        = mock[MongoLockRepository]
    val applicationEventsRepository: ApplicationEventsRepository       = mock[ApplicationEventsRepository]
    val notificationsRepository: NotificationsRepository               = mock[NotificationsRepository]
    val emailConnector: EmailConnector                                 = mock[EmailConnector]
    val thirdPartyApplicationConnector: ThirdPartyApplicationConnector = mock[ThirdPartyApplicationConnector]
    reset(mockAppConfig)
    reset(mockLockRepository)
    when(mockAppConfig.config).thenReturn(mockConfiguration)

    when(mockConfiguration.get[String](eqTo(s"schedules.SendEventNotificationsJob.mongoLockTimeout"))(*)).thenReturn("20seconds")

    val job = new SendEventNotificationsService(
      mockAppConfig,
      applicationEventsRepository,
      mockLockRepository,
      notificationsRepository,
      emailConnector,
      thirdPartyApplicationConnector,
      clock
    )

    val mongoLockTimeout          = "20 seconds"
    val mongoLockId               = s"schedules.${job.jobName}"
    val releaseDuration: Duration = Duration.apply(mongoLockTimeout)

    val event: ApplicationEvent = ApplicationEvents.PpnsCallBackUriUpdatedEvent(
      EventId.random,
      ApplicationId.random,
      instant(),
      Actors.GatekeeperUser("Gatekeeper Admin"),
      "boxId",
      "boxName",
      "https://example.com/old",
      "https://example.com/new"
    )

    def primeLockRepository() = {
      when(mockLockRepository.takeLock(eqTo(mongoLockId), *, *))
        .thenReturn(Future.successful(true))
      when(mockLockRepository.releaseLock(eqTo(mongoLockId), *))
        .thenReturn(Future.successful(()))
    }

    val adminEmail  = "jd@exmample.com"
    val application = ApplicationResponse("test app", Set(Collaborators.Administrator(UserId.random, LaxEmailAddress(adminEmail))))

    def primeApplicationConnectorSuccess(): Unit = {
      when(thirdPartyApplicationConnector.getApplication(eqTo(event.applicationId))(*)).thenReturn(successful(application))
    }

    def primeApplicationConnectorFailed(): Unit = {
      when(thirdPartyApplicationConnector.getApplication(eqTo(event.applicationId))(*)).thenReturn(failed(UpstreamErrorResponse("", NOT_FOUND)))
    }

    def primeEmailConnectorSuccess() = {
      when(emailConnector.sendPpnsCallbackUrlChangedNotification(*, *, *)(*)).thenReturn(successful(HttpResponse(OK, body = "")))
    }

    def primeNotificationsRepositorySuccess(expectedNotification: Notification) = {
      when(notificationsRepository.createEntity(eqTo(expectedNotification))).thenReturn(successful(true))
    }

    def primeApplicationEventsRepositorySuccess(events: ApplicationEvent*): Unit = {
      when(applicationEventsRepository.fetchEventsToNotify())
        .thenReturn(Future.successful(events.toList))
    }

    def primeApplicationEventsRepositoryFailure(): Unit = {
      when(applicationEventsRepository.fetchEventsToNotify())
        .thenReturn(Future.failed(new MongoException("Something went wrong")))
    }

    def verifyNotificationStatus(status: NotificationStatus) = {
      val eventCaptor = ArgCaptor[Notification]
      verify(notificationsRepository).createEntity(eventCaptor)
      eventCaptor.value match {
        case Notification(_, _, status) => succeed
        case n                          => fail(s"Wrong notification status ${n.status}")
      }
    }
  }

  "invoke" should {

    "return true when events are processed and emails sent" in new Setup {
      primeApplicationEventsRepositorySuccess(event)
      primeLockRepository()
      primeApplicationConnectorSuccess()
      primeEmailConnectorSuccess()

      val notification = Notification(event.id, now(), SENT)

      primeNotificationsRepositorySuccess(notification)

      val result = await(job.invoke)
      result match {
        case Right(resultVal) => resultVal mustBe true
        case _                => fail()
      }

      verify(applicationEventsRepository).fetchEventsToNotify()
      verify(thirdPartyApplicationConnector).getApplication(*[ApplicationId])(*)
      verify(emailConnector).sendPpnsCallbackUrlChangedNotification(*, *, *)(*)
      verifyNotificationStatus(SENT)
    }

    "return true when there are no events for application" in new Setup {
      primeApplicationEventsRepositorySuccess()
      primeLockRepository()
      val result: Either[ScheduleStatus.JobFailed, Boolean] = await(job.invoke)
      result match {
        case Right(resultVal) => resultVal mustBe true
        case _                => fail()
      }

      verify(applicationEventsRepository).fetchEventsToNotify()
      verifyZeroInteractions(thirdPartyApplicationConnector)
      verifyZeroInteractions(emailConnector)
      verifyZeroInteractions(notificationsRepository)
    }

    "return true when there are events but no matching applications" in new Setup {
      primeApplicationEventsRepositorySuccess(event)
      primeLockRepository()
      primeApplicationConnectorFailed()

      val notification = Notification(event.id, now(), FAILED)

      primeNotificationsRepositorySuccess(notification)

      val result = await(job.invoke)
      result match {
        case Right(resultVal) => resultVal mustBe true
        case _                => fail()
      }

      verify(applicationEventsRepository).fetchEventsToNotify()
      verify(thirdPartyApplicationConnector).getApplication(*[ApplicationId])(*)
      verifyZeroInteractions(emailConnector)
      verifyNotificationStatus(FAILED)
    }

    // Need failure scenarios here
  }

  "tryLock" should {

    "return the result of the future passed in because the lockRepository was able to lock and unlock successfully" in new Setup {
      val future: Future[Right[Nothing, Boolean]] = Future.successful(Right(true))
      when(mockLockRepository.takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration)))
        .thenReturn(Future.successful(true))
      when(mockLockRepository.releaseLock(eqTo(mongoLockId), *))
        .thenReturn(Future.successful(()))

      await(job.tryLock(future)) mustBe Right(true)

      verify(mockLockRepository, times(1)).takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration))
      verify(mockLockRepository, times(1)).releaseLock(eqTo(mongoLockId), *)
    }

    s"return $Right false if lock returns Future successful false" in new Setup {
      val future: Future[Right[Nothing, Boolean]] = Future.successful(Right(true))
      when(mockLockRepository.takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration)))
        .thenReturn(Future.successful(false))
      await(job.tryLock(future)) mustBe Right(false)

      verify(mockLockRepository, times(1)).takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration))
      verify(mockLockRepository, times(0)).releaseLock(eqTo(mongoLockId), *)
    }

    s"return $Left ${ScheduleStatus.MongoUnlockException} if lock returns exception," +
      s"release lock is still called and succeeds" in new Setup() {
        val future: Future[Right[Nothing, Boolean]] = Future.successful(Right(false))
        val exception                               = new Exception("uh oh")
        when(mockLockRepository.takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration)))
          .thenReturn(Future.failed(exception))
        when(mockLockRepository.releaseLock(eqTo(mongoLockId), *))
          .thenReturn(Future.successful(()))
        await(job.tryLock(future)) mustBe Left(ScheduleStatus.MongoUnlockException(exception))

        verify(mockLockRepository, times(1)).takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration))
        verify(mockLockRepository, times(1)).releaseLock(eqTo(mongoLockId), *)
      }

    s"return $Left ${ScheduleStatus.MongoUnlockException} if lock returns exception," +
      s"release lock is still called and failed also" in new Setup {
        val future: Future[Right[Nothing, Boolean]] = Future.successful(Right(false))
        val exception                               = new Exception("uh oh")
        when(mockLockRepository.takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration)))
          .thenReturn(Future.failed(exception))
        when(mockLockRepository.releaseLock(eqTo(mongoLockId), *))
          .thenReturn(Future.failed(exception))
        await(job.tryLock(future)) mustBe Left(ScheduleStatus.MongoUnlockException(exception))

        verify(mockLockRepository, times(1)).takeLock(eqTo(mongoLockId), *, eqTo(releaseDuration))
        verify(mockLockRepository, times(1)).releaseLock(eqTo(mongoLockId), *)
      }
  }

}
