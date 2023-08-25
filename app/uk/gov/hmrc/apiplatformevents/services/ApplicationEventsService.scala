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

package uk.gov.hmrc.apiplatformevents.services

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Singleton

import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._

import uk.gov.hmrc.apiplatformevents.models.QueryableValues
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository

@Singleton
class ApplicationEventsService @Inject() (repo: ApplicationEventsRepository)(implicit ec: ExecutionContext) {
  def captureEvent[A <: ApplicationEvent](event: A): Future[Boolean] = {
    repo.createEntity(event)
  }

  def fetchEventsBy(applicationId: ApplicationId, eventTag: Option[EventTag], actorType: Option[ActorType]): Future[List[ApplicationEvent]] = {
    repo
      .fetchEvents(applicationId)
      .map(_.filter(event => eventTag.fold(true)(tag => EventTags.tag(event) == tag)))
      .map(_.filter(event => actorType.fold(true)(t => ActorTypes.actorType(event.actor) == t)))
  }

  def fetchEventQueryValues(applicationId: ApplicationId): Future[Option[QueryableValues]] = {
    // Not the most efficient but certainly the more readable
    def handleEvents(events: Seq[ApplicationEvent]): Option[QueryableValues] = {
      if (events.isEmpty) {
        None
      } else {
        val distinctEventTags  = events.map(EventTags.tag).distinct.toList
        val distinctActorTypes = events.map(_.actor).map(ActorTypes.actorType).distinct.toList
        Some(QueryableValues(distinctEventTags, distinctActorTypes))
      }
    }

    for {
      events <- repo.fetchEvents(applicationId)
    } yield handleEvents(events)
  }
}
