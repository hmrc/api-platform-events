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

package uk.gov.hmrc.apiplatformevents.scheduler.jobs

import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.scalatest.MockitoSugar
import org.mongodb.scala.MongoException
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType, EventId, EventType}
import uk.gov.hmrc.apiplatformevents.models.MongoFormatters.PpnsCallBackUriUpdatedEventFormats
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.models.Role.ADMINISTRATOR
import uk.gov.hmrc.apiplatformevents.models.{ApplicationResponse, Collaborator, Notification, PpnsCallBackUriUpdatedEvent}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit
import scala.concurrent.{Future, duration}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{failed, successful}
import uk.gov.hmrc.apiplatformevents.models.ApplicationEvent

class SendEventNotificationsNewJobSpec extends PlaySpec with MockitoSugar with FutureAwaits with DefaultAwaitTimeout with LogCapturing {


  val finiteDuration: FiniteDuration = Duration(4, TimeUnit.MINUTES)

  class Setup {
    val mockAppConfig: AppConfig = mock[AppConfig]
    val mockLockRepository: MongoLockRepository = mock[MongoLockRepository]
    val applicationEventsRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]
    val notificationsRepository: NotificationsRepository = mock[NotificationsRepository]
    val emailConnector: EmailConnector = mock[EmailConnector]
    val thirdPartyApplicationConnector: ThirdPartyApplicationConnector = mock[ThirdPartyApplicationConnector]
    val clock: Clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    reset(mockAppConfig)

    when(mockAppConfig.mongoLockTimeoutDuration(ArgumentMatchers.any()))
      .thenReturn(finiteDuration)

    val job = new SendEventNotificationsNewJob(mockAppConfig,mockLockRepository,
      applicationEventsRepository,
      notificationsRepository,
      emailConnector,
      thirdPartyApplicationConnector,
      clock)

    val mongoLockTimeout = "123seconds"
    val mongoLockId = s"schedules.${job.jobName}"
    val releaseDuration: org.joda.time.Duration = org.joda.time.Duration.standardSeconds(duration.Duration(mongoLockTimeout).toSeconds)

    val event = PpnsCallBackUriUpdatedEvent(EventId.random, "appId", LocalDateTime.now(), Actor("iam@admin.com", ActorType.GATEKEEPER),
      "boxId", "boxName", "https://example.com/old", "https://example.com/new")


    when(mockLockRepository.takeLock(ArgumentMatchers.eq(mongoLockId), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(true))
    when(mockLockRepository.releaseLock(ArgumentMatchers.eq(mongoLockId), ArgumentMatchers.any()))
      .thenReturn(Future.successful(()))

    val adminEmail = "jd@exmample.com"
    val application = ApplicationResponse("test app", Set(Collaborator(adminEmail, ADMINISTRATOR)))
    val notificationCaptor: ArgumentCaptor[Notification] = ArgumentCaptor.forClass(classOf[Notification])

    def primeApplicationConnectorSuccess() {
      when(thirdPartyApplicationConnector.getApplication(eqTo(event.applicationId))(*)).thenReturn(successful(application))
    }

    def primeApplicationConnectorFailed() {
      when(thirdPartyApplicationConnector.getApplication(eqTo(event.applicationId))(*)).thenReturn(failed(UpstreamErrorResponse("", NOT_FOUND)))
    }

    def primeEmailConnectorSuccess()= {
      when(emailConnector.sendPpnsCallbackUrlChangedNotification(*, *, *)(*)).thenReturn(successful(HttpResponse(OK, body = "")))
    }

    def primeNotificationsRepositorySuccess(expectedNotification: Notification)= {
   when(notificationsRepository.createEntity(eqTo(expectedNotification))).thenReturn(successful(true))
    }

    def primeApplicationEventsRepositorySuccess(events: Seq[ApplicationEvent]): Unit ={
      when(applicationEventsRepository.fetchEventsToNotify(EventType.PPNS_CALLBACK_URI_UPDATED))
        .thenReturn(Future.successful(events))
    }

    def primeApplicationEventsRepositoryFailure(): Unit ={
      when(applicationEventsRepository.fetchEventsToNotify(EventType.PPNS_CALLBACK_URI_UPDATED))
        .thenReturn(Future.failed(new MongoException("Something went wrong")))
    }
  }

  "invoke" should {

    "return true when events are processed and emails sent" in new Setup {
      primeApplicationEventsRepositorySuccess(Seq(event))
      primeApplicationConnectorSuccess()
      primeEmailConnectorSuccess()

      val notification = Notification(event.id, LocalDateTime.now(clock), SENT)

      primeNotificationsRepositorySuccess(notification)

      val result =  await(job.invoke)
      result mustBe true

      verify(applicationEventsRepository).fetchEventsToNotify(*)
      verify(thirdPartyApplicationConnector).getApplication(*)(*)
      verify(emailConnector).sendPpnsCallbackUrlChangedNotification(*, *, *)(*)
      verify(notificationsRepository).createEntity(*)
    }

    "return true when there are no events for application" in new Setup {
      primeApplicationEventsRepositorySuccess(Seq.empty)

      val result =  await(job.invoke)
      result mustBe true

      verify(applicationEventsRepository).fetchEventsToNotify(*)
      verifyZeroInteractions(thirdPartyApplicationConnector)
      verifyZeroInteractions(emailConnector)
      verifyZeroInteractions(notificationsRepository)
    }

    "return true when there are events but no matching applications" in new Setup {
      primeApplicationEventsRepositorySuccess(Seq(event))
      primeApplicationConnectorFailed()

      val notification = Notification(event.id, LocalDateTime.now(clock), FAILED)

      primeNotificationsRepositorySuccess(notification)

      val result =  await(job.invoke)
      result mustBe true

      verify(applicationEventsRepository).fetchEventsToNotify(*)
      verify(thirdPartyApplicationConnector).getApplication(*)(*)
      verifyZeroInteractions(emailConnector)
      verifyZeroInteractions(notificationsRepository)
    }
  }

}
