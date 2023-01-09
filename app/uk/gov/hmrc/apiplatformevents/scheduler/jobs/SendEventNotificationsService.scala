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

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, duration}
import scala.util.control.NonFatal

import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.{AbstractApplicationEvent, PpnsCallBackUriUpdatedEvent}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.{LockService, MongoLockRepository}

import uk.gov.hmrc.apiplatformevents.connectors.{EmailConnector, ThirdPartyApplicationConnector}
import uk.gov.hmrc.apiplatformevents.models.Notification
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.{FAILED, SENT}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.apiplatformevents.scheduler.ScheduleStatus.{MongoUnlockException, UnknownExceptionOccurred}
import uk.gov.hmrc.apiplatformevents.scheduler.{ScheduleStatus, ScheduledService}
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig

class SendEventNotificationsService @Inject() (
    appConfig: AppConfig,
    applicationEventsRepository: ApplicationEventsRepository,
    lockRepositoryProvider: MongoLockRepository,
    notificationsRepository: NotificationsRepository,
    emailConnector: EmailConnector,
    thirdPartyApplicationConnector: ThirdPartyApplicationConnector,
    clock: Clock
) extends ScheduledService[Either[ScheduleStatus.JobFailed, Boolean]]
    with ApplicationLogger {
  val jobName: String = "SendEventNotificationsJob"

  lazy val mongoLockTimeoutSeconds: duration.Duration = duration.Duration(
    appConfig.config.get[String](s"schedules.$jobName.mongoLockTimeout")
  )

  lazy val lockKeeper = LockService(
    lockRepository = lockRepositoryProvider,
    lockId = s"schedules.SendEventNotificationsJob",
    ttl = mongoLockTimeoutSeconds
  )

  def tryLock(f: => Future[Either[ScheduleStatus.JobFailed, Boolean]])(implicit ec: ExecutionContext): Future[Either[ScheduleStatus.JobFailed, Boolean]] = {
    lockKeeper
      .withLock(f)
      .map {
        case Some(result) => result
        case None         =>
          logger.info(s"$jobName locked because it might be running on another instance")
          Right(false)
      }
      .recover { case e: Exception =>
        Left(MongoUnlockException(e))
      }
  }

  override def invoke(implicit ec: ExecutionContext): Future[Either[ScheduleStatus.JobFailed, Boolean]] = {
    tryLock {
      logger.info(s"[$jobName Scheduled Job Started]")
      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      applicationEventsRepository
        .fetchEventsToNotify[PpnsCallBackUriUpdatedEvent]()
        .flatMap(events =>
          if (events.isEmpty) {
            logger.info("SendEventNotificationsJob no events to process")
            Future.successful(Seq.empty)
          } else { processEvents(events) }
        )
        .map(_ => {
          logger.info("SendEventNotificationsJob Successful")
          Right(true)
        })
        .recoverWith { case NonFatal(e) =>
          logger.error(s"$jobName failed ${e.getMessage}")
          Future.successful(Left(UnknownExceptionOccurred(e)))
        }

    }
  }

  private def processEvents(events: Seq[AbstractApplicationEvent])(implicit ec: ExecutionContext, hc: HeaderCarrier) = {
    Future.sequence {
      events.map(sendEventNotification)
    }
  }

  private def sendEventNotification(event: AbstractApplicationEvent)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Unit] = {
    logger.info(s"processing event: ${event.id}")
    event match {
      case ppnsEvent: PpnsCallBackUriUpdatedEvent =>
        (for {
          app <- thirdPartyApplicationConnector.getApplication(ppnsEvent.applicationId)
          _   <- emailConnector.sendPpnsCallbackUrlChangedNotification(app.name, ppnsEvent.eventDateTime, app.adminEmails)
          _   <- notificationsRepository.createEntity(Notification(ppnsEvent.id, LocalDateTime.now(clock), SENT))
        } yield ()) recoverWith { case NonFatal(e) =>
          logger.error(s"Failed to send email notification for event ID ${ppnsEvent.id}", e)
          notificationsRepository.createEntity(Notification(ppnsEvent.id, LocalDateTime.now(clock), FAILED)).map(_ => ())
        }
      case _                                      => Future.successful(logger.error(s"Event not of correct type to send notification ${event.getClass.getSimpleName}"))
    }

  }
}
