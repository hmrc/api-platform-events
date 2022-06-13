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

import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.models.common.EventType.PPNS_CALLBACK_URI_UPDATED
import uk.gov.hmrc.apiplatformevents.models.{ApplicationEvent, Notification, PpnsCallBackUriUpdatedEvent}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.apiplatformevents.scheduler.{Locking, ScheduledService}
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, duration}
import scala.util.control.NonFatal

class SendEventNotificationsNewJob @Inject()(appConfig: AppConfig,
                                              override val lockRepository: MongoLockRepository,
                                             applicationEventsRepository: ApplicationEventsRepository,
                                             notificationsRepository: NotificationsRepository,
                                             emailConnector: EmailConnector,
                                             thirdPartyApplicationConnector: ThirdPartyApplicationConnector,
                                             clock: Clock) extends ScheduledService[Boolean] with Locking {
  override val jobName: String = "SendEventNotificationsJob"

  override val ttl: duration.Duration = appConfig.mongoLockTimeoutDuration(Some(jobName))

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] = {
    tryLock {
      logger.info(s"[$jobName Scheduled Job Started]")
      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      applicationEventsRepository.fetchEventsToNotify[PpnsCallBackUriUpdatedEvent](PPNS_CALLBACK_URI_UPDATED)
        .flatMap(processEvents)
        .map(_ => {logger.info("SendEventNotificationsJob Successful")
                    true}
        ).recoverWith {
          case NonFatal(e) =>
            logger.error(s"$jobName failed ${e.getMessage}")
            Future.successful(false)
        }

    }
  }

  private def processEvents(events: Seq[ApplicationEvent])(implicit ec: ExecutionContext, hc: HeaderCarrier) = {
      Future.sequence{
        events.map(sendEventNotification)
      }
  }

  private def sendEventNotification(event: ApplicationEvent)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Unit] = {
    event match {
         case ppnsEvent: PpnsCallBackUriUpdatedEvent =>
                                      (for {
                                        app <- thirdPartyApplicationConnector.getApplication(ppnsEvent.applicationId)
                                        _ <- emailConnector.sendPpnsCallbackUrlChangedNotification(app.name, ppnsEvent.eventDateTime, app.adminEmails)
                                        _ <- notificationsRepository.createEntity(Notification(ppnsEvent.id, LocalDateTime.now(clock), SENT))
                                      } yield ()) recoverWith {
                                        case NonFatal(e) =>
                                          logger.error(s"Failed to send email notification for event ID ${ppnsEvent.id}", e)
                                          notificationsRepository.createEntity(Notification(ppnsEvent.id, LocalDateTime.now(clock), FAILED)).map(_ => ())
                                      }
         case _ => Future.successful(logger.error(s"Event not of correct type to send notification ${event.eventType}"))
    }

  }
}
