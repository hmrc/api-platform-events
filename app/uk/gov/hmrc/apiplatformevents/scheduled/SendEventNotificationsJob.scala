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

package uk.gov.hmrc.apiplatformevents.scheduled

import akka.stream.Materializer
import com.google.inject.Singleton

import java.time.LocalDateTime
import scala.concurrent.duration.{Duration, MINUTES}
import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.MongoFormatters.PpnsCallBackUriUpdatedEventFormats
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.models.common.EventType.PPNS_CALLBACK_URI_UPDATED
import uk.gov.hmrc.apiplatformevents.models.{Notification, PpnsCallBackUriUpdatedEvent}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SendEventNotificationsJob @Inject()(
  lockKeeper: SendEventNotificationsJobLockKeeper,
  jobConfig: SendEventNotificationsJobConfig,
  override val lockRepository: MongoLockRepository,
  applicationEventsRepository: ApplicationEventsRepository,
  notificationsRepository: NotificationsRepository,
  emailConnector: EmailConnector,
  thirdPartyApplicationConnector: ThirdPartyApplicationConnector,
  clock: Clock
)(
  implicit mat: Materializer
) extends ScheduledMongoJob with ApplicationLogger {
  override def name: String = "SendEventNotificationsJob"
  override def interval: FiniteDuration = jobConfig.interval
  override def initialDelay: FiniteDuration = jobConfig.initialDelay
  override val isEnabled: Boolean = jobConfig.enabled
  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def runJob(implicit ec: ExecutionContext): Future[RunningOfJobSuccessful] = {
    applicationEventsRepository
      .fetchEventsToNotify[PpnsCallBackUriUpdatedEvent](PPNS_CALLBACK_URI_UPDATED)
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
      _ <- notificationsRepository.createEntity(Notification(event.id, LocalDateTime.now(clock), SENT))
    } yield ()) recoverWith {
      case NonFatal(e) =>
        logger.error(s"Failed to send email notification for event ID ${event.id}", e)
        notificationsRepository.createEntity(Notification(event.id, LocalDateTime.now(clock), FAILED)).map(_ => ())
    }
  }
}

class SendEventNotificationsJobLockKeeper @Inject()()  {


 def lockId: String = "SendEventNotificationsJob"

  val forceLockReleaseAfter: Duration = Duration.apply(60, MINUTES) // scalastyle:off magic.number
}

case class SendEventNotificationsJobConfig(initialDelay: FiniteDuration,
                                           interval: FiniteDuration,
                                           enabled: Boolean,
                                           parallelism: Int)
