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

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.google.inject.Singleton
import javax.inject.Inject
import org.joda.time.DateTime.now
import org.joda.time.DateTimeZone.UTC
import org.joda.time.Duration
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.models.ReactiveMongoFormatters.PpnsCallBackUriUpdatedEventFormats
import uk.gov.hmrc.apiplatformevents.models.common.EventType.PPNS_CALLBACK_URI_UPDATED
import uk.gov.hmrc.apiplatformevents.models.{Notification, PpnsCallBackUriUpdatedEvent}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lock.{LockKeeper, LockRepository}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SendEventNotificationsJob @Inject()(override val lockKeeper: SendEventNotificationsJobLockKeeper,
                                          jobConfig: SendEventNotificationsJobConfig,
                                          applicationEventsRepository: ApplicationEventsRepository,
                                          notificationsRepository: NotificationsRepository,
                                          emailConnector: EmailConnector,
                                          thirdPartyApplicationConnector: ThirdPartyApplicationConnector)
                                         (implicit mat: Materializer) extends ScheduledMongoJob {

  override def name: String = "SendEventNotificationsJob"
  override def interval: FiniteDuration = jobConfig.interval
  override def initialDelay: FiniteDuration = jobConfig.initialDelay
  override val isEnabled: Boolean = jobConfig.enabled
  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def runJob(implicit ec: ExecutionContext): Future[RunningOfJobSuccessful] = {
    applicationEventsRepository
      .fetchEventsToNotify[PpnsCallBackUriUpdatedEvent](PPNS_CALLBACK_URI_UPDATED)
      .runWith(Sink.foreachAsync[PpnsCallBackUriUpdatedEvent](jobConfig.parallelism)(sendEventNotification))
      .map(_ => RunningOfJobSuccessful)
      .recoverWith {
        case NonFatal(e) =>
          Future.failed(RunningOfJobFailed(name, e))
      }
  }

  private def sendEventNotification(event: PpnsCallBackUriUpdatedEvent)(implicit ec: ExecutionContext): Future[Unit] = {
    (for {
      app <- thirdPartyApplicationConnector.getApplication(event.applicationId)
      _ <- emailConnector.sendPpnsCallbackUrlChangedNotification(app.name, event.eventDateTime, app.adminEmails)
      _ <- notificationsRepository.createEntity(Notification(event.id, now(UTC), SENT))
    } yield ()) recoverWith {
      case NonFatal(e) =>
        Logger.error(s"Failed to send email notification for event ID ${event.id}", e)
        notificationsRepository.createEntity(Notification(event.id, now(UTC), FAILED)).map(_ => ())
    }
  }
}

class SendEventNotificationsJobLockKeeper @Inject()(mongo: ReactiveMongoComponent) extends LockKeeper {
  override def repo: LockRepository = new LockRepository()(mongo.mongoConnector.db)

  override def lockId: String = "SendEventNotificationsJob"

  override val forceLockReleaseAfter: Duration = Duration.standardMinutes(60) // scalastyle:off magic.number
}

case class SendEventNotificationsJobConfig(initialDelay: FiniteDuration,
                                           interval: FiniteDuration,
                                           enabled: Boolean,
                                           parallelism: Int)
