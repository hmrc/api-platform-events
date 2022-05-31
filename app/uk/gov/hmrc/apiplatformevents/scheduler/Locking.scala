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

package uk.gov.hmrc.apiplatformevents.scheduler

import play.api.Logging
import uk.gov.hmrc.mongo.lock.{LockService, MongoLockRepository}

import scala.concurrent.{ExecutionContext, Future, duration}

trait Locking extends LockService with Logging {

  val jobName: String

  override val lockRepository: MongoLockRepository
  override val ttl: duration.Duration
  override lazy val lockId: String = s"schedules.$jobName"

  def tryLock(f: => Future[Boolean])(implicit ec: ExecutionContext): Future[Boolean] = {
    super.withLock(f).map {
      case Some(result) =>
        result
      case None =>
        logger.info(
          s"$jobName locked because it might be running on another instance (This is expected if your microservice has more than one instance running)"
        )
        false
    }.recover {
      case e: Exception =>
        logger.info(s"$jobName failed with exception ${e.getMessage}")
        throw e
    }
  }

}