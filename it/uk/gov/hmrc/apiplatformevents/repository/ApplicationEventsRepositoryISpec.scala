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

import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.SENT
import uk.gov.hmrc.apiplatformevents.models._

import java.time.LocalDateTime
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import uk.gov.hmrc.apiplatformevents.support.ServerBaseISpec
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventId
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.AbstractApplicationEvent


class ApplicationEventsRepositoryISpec extends ServerBaseISpec with BeforeAndAfterEach with ApplicationEventTestData {

  override protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  val repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]
  val notificationsRepo: NotificationsRepository = app.injector.instanceOf[NotificationsRepository]

  override def beforeEach(): Unit = {
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

    "create a collaboratorRemoved entity" in {
      await(repo.createEntity(collaboratorRemoved))
      await(repo.collection.find().toFuture()) should contain only collaboratorRemoved
    }

    "create a teamMemberAdded entity" in {
      await(repo.createEntity(teamMemberAddedModel))
      await(repo.collection.find().toFuture()) should contain only teamMemberAddedModel
    }

    "create a collaboratorAdded entity" in {
      await(repo.createEntity(collaboratorAdded))
      await(repo.collection.find().toFuture()) should contain only collaboratorAdded
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

    "create a productionAppNameChangedEvent entity" in {
      await(repo.createEntity(productionAppNameChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionAppNameChangedEvent
    }

    "create a productionAppPrivacyPolicyLocationChanged entity" in {
      await(repo.createEntity(productionAppPrivacyPolicyLocationChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionAppPrivacyPolicyLocationChangedEvent
    }

    "create a productionLegacyAppPrivacyPolicyLocationChangedEvent entity" in {
      await(repo.createEntity(productionLegacyAppPrivacyPolicyLocationChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionLegacyAppPrivacyPolicyLocationChangedEvent
    }

    "create a productionAppTermsConditionsLocationChangedEvent entity" in {
      await(repo.createEntity(productionAppTermsConditionsLocationChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionAppTermsConditionsLocationChangedEvent
    }

    "create a productionLegacyAppTermsConditionsLocationChangedEvent entity" in {
      await(repo.createEntity(productionLegacyAppTermsConditionsLocationChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionLegacyAppTermsConditionsLocationChangedEvent
    }

    "create a responsibleIndividualChangedEvent entity" in {
      await(repo.createEntity(responsibleIndividualChangedEvent))
      await(repo.collection.find().toFuture()) should contain only responsibleIndividualChangedEvent
    }

    "create a responsibleIndividualSetEvent entity" in {
      await(repo.createEntity(responsibleIndividualSetEvent))
      await(repo.collection.find().toFuture()) should contain only responsibleIndividualSetEvent
    }

    "create a applicationStateChangedEvent entity" in {
      await(repo.createEntity(applicationStateChangedEvent))
      await(repo.collection.find().toFuture()) should contain only applicationStateChangedEvent
    }

    "create a responsibleIndividualVerificationStarted entity" in {
      await(repo.createEntity(responsibleIndividualVerificationStarted))
      await(repo.collection.find().toFuture()) should contain only responsibleIndividualVerificationStarted
    }

    "create a responsibleIndividualDeclinedEvent entity" in {
      await(repo.createEntity(responsibleIndividualDeclinedEvent))
      await(repo.collection.find().toFuture()) should contain only responsibleIndividualDeclinedEvent
    }

    "create a responsibleIndividualDeclinedUpdateEvent entity" in {
      await(repo.createEntity(responsibleIndividualDeclinedUpdateEvent))
      await(repo.collection.find().toFuture()) should contain only responsibleIndividualDeclinedUpdateEvent
    }

    "create a responsibleIndividualDidNotVerifyEvent entity" in {
      await(repo.createEntity(responsibleIndividualDidNotVerifyEvent))
      await(repo.collection.find().toFuture()) should contain only responsibleIndividualDidNotVerifyEvent
    }

    "create a applicationApprovalRequestDeclinedEvent entity" in {
      await(repo.createEntity(applicationApprovalRequestDeclinedEvent))
      await(repo.collection.find().toFuture()) should contain only applicationApprovalRequestDeclinedEvent
    }

  }

  "fetchEventsToNotify" should {
    "filter results" in {
      await(repo.createEntity(teamMemberAddedModel))
      await(repo.createEntity(clientSecretAddedModel))
      await(repo.createEntity(ppnsCallBackUriUpdatedEvent))

      val result: List[AbstractApplicationEvent] = await(repo.fetchEventsToNotify())

      result should contain only ppnsCallBackUriUpdatedEvent
    }

    "only return events that have not been notified yet" in {
      await(repo.createEntity(ppnsCallBackUriUpdatedEvent))
      val anotherEvent = ppnsCallBackUriUpdatedEvent.copy(id = EventId.random)
      await(repo.createEntity(anotherEvent))
      val alreadyNotifiedEvent = ppnsCallBackUriUpdatedEvent.copy(id = EventId.random)
      await(repo.createEntity(alreadyNotifiedEvent))
      await(notificationsRepo.createEntity(Notification(alreadyNotifiedEvent.id, LocalDateTime.now(), SENT)))

      val result: List[AbstractApplicationEvent] = await(repo.fetchEventsToNotify())

      result should contain theSameElementsAs List(ppnsCallBackUriUpdatedEvent, anotherEvent)
    }
  }
}
