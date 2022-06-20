/*
 * Copyright 2017 HM Revenue & Customs
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
package uk.gov.hmrc.apiplatformevents.repository

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.SENT
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.OldEventType.TEAM_MEMBER_ADDED
import uk.gov.hmrc.apiplatformevents.models.common.EventId
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

import java.time.LocalDateTime
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData

class ApplicationEventsRepositoryISpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ApplicationEventTestData {

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  val repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]
  val notificationsRepo: NotificationsRepository = app.injector.instanceOf[NotificationsRepository]


  override def beforeEach() {
    await(notificationsRepo.collection.drop().toFuture())
    await(notificationsRepo.ensureIndexes)
    await(repo.collection.drop().toFuture())
    await(repo.ensureIndexes)

  }


  "createEntity" should {
    "create a teamMemberRemoved entity" in {
      await(repo.createEntity(teamMemberRemovedModel))
      await(repo.collection.find().toFuture()) should contain only teamMemberRemovedModel
    }

    "create a teamMemberAdded entity" in {
      await(repo.createEntity(teamMemberAddedModel))
      await(repo.collection.find().toFuture()) should contain only teamMemberAddedModel
    }

    "create a clientSecretAdded entity" in {
      await(repo.createEntity(clientSecretAddedModel))
      await(repo.collection.find().toFuture()) should contain only clientSecretAddedModel
    }

    "create a clientSecretRemoved entity" in {
      await(repo.createEntity(clientSecretRemovedModel))
      await(repo.collection.find().toFuture()) should contain only clientSecretRemovedModel
    }

    "create a redirectUrisUpdated entity" in {
      await(repo.createEntity(redirectUrisUpdatedModel))
      await(repo.collection.find().toFuture()) should contain only redirectUrisUpdatedModel
    }

    "create an apiSubsribed entity" in {
      await(repo.createEntity(apiSubscribedModel))
      await(repo.collection.find().toFuture()) should contain only apiSubscribedModel
    }

    "create an apiUnsubsribed entity" in {
      await(repo.createEntity(apiUnsubscribedModel))
      await(repo.collection.find().toFuture()) should contain only apiUnsubscribedModel
    }

    "create an productionAppNameChangedEvent entity" in {
      await(repo.createEntity(productionAppNameChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionAppNameChangedEvent
    }
  }

  "fetchEventsToNotify" should {
    "filter results by event type" in {
      await(repo.createEntity(teamMemberAddedModel))
      await(repo.createEntity(clientSecretAddedModel))

      val result: Seq[OldApplicationEvent] = await(repo.fetchEventsToNotify(TEAM_MEMBER_ADDED))

      result should contain only teamMemberAddedModel
    }

    "only return events that have not been notified yet" in {
      await(repo.createEntity(teamMemberAddedModel))
      val anotherTeamMemberAddedModel = teamMemberAddedModel.copy(id = EventId.random)
      await(repo.createEntity(anotherTeamMemberAddedModel))
      val alreadyNotifiedTeamMemberAddedModel = teamMemberAddedModel.copy(id = EventId.random)
      await(repo.createEntity(alreadyNotifiedTeamMemberAddedModel))
      await(notificationsRepo.createEntity(Notification(alreadyNotifiedTeamMemberAddedModel.id, LocalDateTime.now(), SENT)))

      val result: Seq[OldApplicationEvent] = await(repo.fetchEventsToNotify(TEAM_MEMBER_ADDED))

      result should contain only (teamMemberAddedModel, anotherTeamMemberAddedModel)
    }
  }
}
