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

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ApplicationEventsService @Inject()(repo: ApplicationEventsRepository) {

  def captureEvent(event: TeamMemberAddedEvent)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event).recover {
      case NonFatal(e) => {
        Logger.info("Exception happened when trying to createEntity:",e)
        false
      }
    }
  }

}
