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
import uk.gov.hmrc.apiplatformevents.models.{ApiSubscribedEvent, ApiUnsubscribedEvent, ClientSecretAddedEvent, ClientSecretRemovedEvent, RedirectUrisUpdatedEvent, TeamMemberAddedEvent, TeamMemberRemovedEvent}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationEventsService @Inject()(repo: ApplicationEventsRepository) {

  def captureTeamMemberAddedEvent(event: TeamMemberAddedEvent)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

  def captureTeamMemberRemovedEvent(event: TeamMemberRemovedEvent)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

  def captureClientSecretAddedEvent(event: ClientSecretAddedEvent)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

  def captureClientSecretRemovedEvent(event: ClientSecretRemovedEvent)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

  def captureRedirectUrisUpdatedEvent(event: RedirectUrisUpdatedEvent)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

  def captureApiSubscribedEvent(event: ApiSubscribedEvent)(
  implicit hc: HeaderCarrier,
  ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

  def captureApiUnsubscribedEvent(event: ApiUnsubscribedEvent)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Boolean] = {
    repo.createEntity(event)
  }

}
