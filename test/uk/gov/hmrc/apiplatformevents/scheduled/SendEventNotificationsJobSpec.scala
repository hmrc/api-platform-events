/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformevents.scheduled

import java.util.concurrent.TimeUnit.{HOURS, SECONDS}

import akka.stream.Materializer
import akka.stream.scaladsl.Source.{fromFutureSource, fromIterator}
import org.joda.time.DateTime.now
import org.joda.time.DateTimeZone.UTC
import org.joda.time.Duration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.akkastream.State
import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.models.ReactiveMongoFormatters.PpnsCallBackUriUpdatedEventFormats
import uk.gov.hmrc.apiplatformevents.models.Role.ADMINISTRATOR
import uk.gov.hmrc.apiplatformevents.models.common.EventType.PPNS_CALLBACK_URI_UPDATED
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType, EventId}
import uk.gov.hmrc.apiplatformevents.models.{ApplicationResponse, Collaborator, Notification, PpnsCallBackUriUpdatedEvent}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.lock.LockRepository
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class SendEventNotificationsJobSpec extends UnitSpec with MockitoSugar with MongoSpecSupport with GuiceOneAppPerSuite {

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure("metrics.enabled" -> false).build()
  implicit lazy val materializer: Materializer = app.materializer

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val lockKeeperSuccess: () => Boolean = () => true
    private val reactiveMongoComponent = new ReactiveMongoComponent {
      override def mongoConnector: MongoConnector = mongoConnectorForTest
    }
    val mockLockKeeper: SendEventNotificationsJobLockKeeper = new SendEventNotificationsJobLockKeeper(reactiveMongoComponent) {
      override def lockId: String = "testLock"
      override def repo: LockRepository = mock[LockRepository]
      override val forceLockReleaseAfter: Duration = Duration.standardMinutes(5) // scalastyle:off magic.number
      override def tryLock[T](body: => Future[T])(implicit ec: ExecutionContext): Future[Option[T]] =
        if (lockKeeperSuccess()) body.map(value => successful(Some(value)))
        else successful(None)
    }

    val notificationCaptor: ArgumentCaptor[Notification] = ArgumentCaptor.forClass(classOf[Notification])
    val sendEventNotificationsJobConfig: SendEventNotificationsJobConfig = SendEventNotificationsJobConfig(
      FiniteDuration(60, SECONDS), FiniteDuration(24, HOURS), enabled = true, 1)
    val mockApplicationEventsRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]
    val mockNotificationsRepository: NotificationsRepository = mock[NotificationsRepository]
    val mockEmailConnector: EmailConnector = mock[EmailConnector]
    val mockThirdPartyApplicationConnector: ThirdPartyApplicationConnector = mock[ThirdPartyApplicationConnector]
    val underTest = new SendEventNotificationsJob(
      mockLockKeeper,
      sendEventNotificationsJobConfig,
      mockApplicationEventsRepository,
      mockNotificationsRepository,
      mockEmailConnector,
      mockThirdPartyApplicationConnector
    )
  }

  "SendEventNotificationsJob" should {
    import scala.concurrent.ExecutionContext.Implicits.global
    val adminEmail = "jd@exmample.com"
    val application = ApplicationResponse("test app", Set(Collaborator(adminEmail, ADMINISTRATOR)))
    val event = PpnsCallBackUriUpdatedEvent(EventId.random, "appId", now(UTC), Actor("iam@admin.com", ActorType.GATEKEEPER),
      "boxId", "boxName", "https://example.com/old", "https://example.com/new")

    "send email notifications for PPNS_CALLBACK_URI_UPDATED events" in new Setup {
      when(mockApplicationEventsRepository.fetchEventsToNotify[PpnsCallBackUriUpdatedEvent](PPNS_CALLBACK_URI_UPDATED))
        .thenReturn(fromFutureSource(successful(fromIterator(() => Seq(event).toIterator))))
      when(mockThirdPartyApplicationConnector.getApplication(meq(event.applicationId))(any())).thenReturn(application)
      when(mockEmailConnector.sendPpnsCallbackUrlChangedNotification(any(), any(), any())(any())).thenReturn(HttpResponse(OK, body = ""))
      when(mockNotificationsRepository.createEntity(notificationCaptor.capture())).thenReturn(successful(true))

      val result: underTest.Result = await(underTest.execute)

      verify(mockEmailConnector, times(1))
        .sendPpnsCallbackUrlChangedNotification(meq(application.name), meq(event.eventDateTime), meq(Set(adminEmail)))(any())
      notificationCaptor.getValue.eventId shouldBe event.id
      notificationCaptor.getValue.status shouldBe SENT
      result.message shouldBe "SendEventNotificationsJob Job ran successfully."
    }

    "record failed individual notification attempts without failing the stream" in new Setup {
      when(mockApplicationEventsRepository.fetchEventsToNotify[PpnsCallBackUriUpdatedEvent](PPNS_CALLBACK_URI_UPDATED))
        .thenReturn(fromFutureSource(successful(fromIterator(() => Seq(event).toIterator))))
      when(mockThirdPartyApplicationConnector.getApplication(meq(event.applicationId))(any())).thenReturn(application)
      when(mockEmailConnector.sendPpnsCallbackUrlChangedNotification(any(), any(), any())(any())).thenReturn(failed(new RuntimeException("Failed")))
      when(mockNotificationsRepository.createEntity(notificationCaptor.capture())).thenReturn(successful(true))

      val result: underTest.Result = await(underTest.execute)

      notificationCaptor.getValue.eventId shouldBe event.id
      notificationCaptor.getValue.status shouldBe FAILED
      result.message shouldBe "SendEventNotificationsJob Job ran successfully."
    }

    "not execute if the job is already running" in new Setup {
      override val lockKeeperSuccess: () => Boolean = () => false

      val result: underTest.Result = await(underTest.execute)

      verify(mockEmailConnector, never).sendPpnsCallbackUrlChangedNotification(any(), any(), any())(any())
      result.message shouldBe "SendEventNotificationsJob did not run because repository was locked by another instance of the scheduler."
    }

    "fail the job when fetching the events fails" in new Setup {
      when(mockApplicationEventsRepository.fetchEventsToNotify[PpnsCallBackUriUpdatedEvent](PPNS_CALLBACK_URI_UPDATED))
        .thenReturn(fromFutureSource[PpnsCallBackUriUpdatedEvent, State](failed(new RuntimeException("Failed"))))

      val result: underTest.Result = await(underTest.execute)

      verify(mockEmailConnector, never).sendPpnsCallbackUrlChangedNotification(any(), any(), any())(any())
      result.message shouldBe "The execution of scheduled job SendEventNotificationsJob failed with error 'Failed'. " +
        "The next execution of the job will do retry."
    }
  }
}
