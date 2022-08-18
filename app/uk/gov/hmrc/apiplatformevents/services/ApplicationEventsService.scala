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

package uk.gov.hmrc.apiplatformevents.services

import com.google.inject.Singleton
import javax.inject.Inject
import uk.gov.hmrc.apiplatformevents.models.{ApplicationEvent, HasActor, HasOldActor}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository

import scala.concurrent.Future
import uk.gov.hmrc.apiplatformevents.models.common.{ApplicationId, EventType, GatekeeperUserActor, CollaboratorActor}
import scala.concurrent.ExecutionContext

@Singleton
class ApplicationEventsService @Inject()(repo: ApplicationEventsRepository)(implicit ec: ExecutionContext) {
  def captureEvent[A <: ApplicationEvent](event : A): Future[Boolean] ={
    repo.createEntity(event)
  }

  def fetchEventsBy(applicationId: ApplicationId, year: Option[Int], eventType: Option[EventType], actor: Option[String]) = {
    val yearFilter: ApplicationEvent => Boolean = { evt =>
      year.fold(true)(matchYear => evt.eventDateTime.getYear == matchYear)
    }

    val actorFilter: ApplicationEvent => Boolean = { evt =>
      actor.fold(true)(matchActor =>
        evt match {
          case e: ApplicationEvent with HasOldActor => e.actor.id == matchActor
          case ea: ApplicationEvent with HasActor => 
            ea.actor match {
              case GatekeeperUserActor(user) => user == matchActor
              case CollaboratorActor(email) => email == matchActor
              case _ => true
            }
          case _ => true
        }
      )
    }

    repo.fetchEventsBy(applicationId, eventType)
    .map(
      evts => evts.filter(yearFilter).filter(actorFilter)
    )
  }
}
