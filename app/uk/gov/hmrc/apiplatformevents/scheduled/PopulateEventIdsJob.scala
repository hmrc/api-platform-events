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

package uk.gov.hmrc.apiplatformevents.scheduled

import java.util.concurrent.TimeUnit.{HOURS, SECONDS}

import com.google.inject.Singleton
import javax.inject.Inject
import org.joda.time.Duration
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.lock.{LockKeeper, LockRepository}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class PopulateEventIdsJob @Inject()(override val lockKeeper: PopulateEventIdsJobLockKeeper,
                                    applicationEventsRepository: ApplicationEventsRepository) extends ScheduledMongoJob {

  override def name: String = "PopulateEventIdsJob"
  override def interval: FiniteDuration = FiniteDuration(24, HOURS)
  override def initialDelay: FiniteDuration = FiniteDuration(60, SECONDS)
  override val isEnabled: Boolean = true

  override def runJob(implicit ec: ExecutionContext): Future[RunningOfJobSuccessful] = {
    applicationEventsRepository.populateEventIds()
      .map(_ => RunningOfJobSuccessful)
      .recoverWith {
        case NonFatal(e) =>
          Logger.error("Failed to populate event IDs", e)
          Future.failed(RunningOfJobFailed(name, e))
      }
  }
}

class PopulateEventIdsJobLockKeeper @Inject()(mongo: ReactiveMongoComponent) extends LockKeeper {
  override def repo: LockRepository = new LockRepository()(mongo.mongoConnector.db)

  override def lockId: String = "PopulateEventIdsJob"

  override val forceLockReleaseAfter: Duration = Duration.standardMinutes(60) // scalastyle:off magic.number
}
