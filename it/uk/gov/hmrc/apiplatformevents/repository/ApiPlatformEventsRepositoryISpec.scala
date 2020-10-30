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

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.joda.time.DateTime.now
import org.joda.time.DateTimeZone.UTC
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.api.indexes.Index
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.SENT
import uk.gov.hmrc.apiplatformevents.models.ReactiveMongoFormatters.teamMemberAddedEventFormats
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.EventType.TEAM_MEMBER_ADDED
import uk.gov.hmrc.apiplatformevents.models.common.{Actor, ActorType, ApplicationEvent, EventId}
import uk.gov.hmrc.apiplatformevents.support.MongoApp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class ApiPlatformEventsRepositoryISpec extends UnitSpec with MongoApp {

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  override implicit lazy val app: Application = appBuilder.build()
  implicit val mat: Materializer = app.injector.instanceOf[Materializer]

  def repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]
  def notificationsRepo: NotificationsRepository = app.injector.instanceOf[NotificationsRepository]

  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }

  val teamMemberAddedModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    teamMemberEmail = "jkhkhk",
    teamMemberRole = "ADMIN")

  val teamMemberRemovedModel: TeamMemberRemovedEvent = TeamMemberRemovedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    teamMemberEmail = "jkhkhk",
    teamMemberRole = "ADMIN")

  val clientSecretAddedModel: ClientSecretAddedEvent = ClientSecretAddedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    clientSecretId = "jkhkhk")

  val clientSecretRemovedModel: ClientSecretRemovedEvent = ClientSecretRemovedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    clientSecretId = "jkhkhk")

  val redirectUrisUpdatedModel: RedirectUrisUpdatedEvent = RedirectUrisUpdatedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    oldRedirectUris = "oldru",
    newRedirectUris = "newru")

  val apiSubscribedModel: ApiSubscribedEvent = ApiSubscribedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    context = "apicontext",
    version = "1.0")

  val apiUnsubscribedModel: ApiUnsubscribedEvent = ApiUnsubscribedEvent(
    id = EventId.random,
    applicationId = "John Smith",
    eventDateTime = now(UTC),
    Actor("iam@admin.com", ActorType.GATEKEEPER),
    context = "apicontext",
    version = "1.0")

  def getIndex(indexName: String): Option[Index] ={
    await(repo.collection.indexesManager.list().map(_.find(_.eventualName.equalsIgnoreCase(indexName))))
  }

  "Indexes" should {
    "create unique ID index"in {
      val Some(index) = getIndex("id_index")
      index.unique shouldBe true
    }
  }

  "createEntity" should {
    "create a teamMemberAdded entity" in {
      await(repo.createEntity(teamMemberAddedModel))
      await(repo.find()) should contain only teamMemberAddedModel
    }

    "create a teamMemberRemoved entity" in {
      await(repo.createEntity(teamMemberRemovedModel))
      await(repo.find()) should contain only teamMemberRemovedModel
    }

    "create a clientSecretAdded entity" in {
      await(repo.createEntity(clientSecretAddedModel))
      await(repo.find()) should contain only clientSecretAddedModel
    }

    "create a clientSecretRemoved entity" in {
      await(repo.createEntity(clientSecretRemovedModel))
      await(repo.find()) should contain only clientSecretRemovedModel
    }

    "create a redirectUrisUpdated entity" in {
      await(repo.createEntity(redirectUrisUpdatedModel))
      await(repo.find()) should contain only redirectUrisUpdatedModel
    }

    "create an apiSubsribed entity" in {
      await(repo.createEntity(apiSubscribedModel))
      await(repo.find()) should contain only apiSubscribedModel
    }

    "create an apiUnsubsribed entity" in {
      await(repo.createEntity(apiUnsubscribedModel))
      await(repo.find()) should contain only apiUnsubscribedModel
    }
  }

  "fetchEventsToNotify" should {
    "filter results by event type" in {
      await(repo.createEntity(teamMemberAddedModel))
      await(repo.createEntity(clientSecretAddedModel))

      val result: Seq[ApplicationEvent] = await(repo.fetchEventsToNotify(TEAM_MEMBER_ADDED).runWith(Sink.seq))

      result should contain only teamMemberAddedModel
    }

    "only return events that have not been notified yet" in {
      await(repo.createEntity(teamMemberAddedModel))
      val anotherTeamMemberAddedModel = teamMemberAddedModel.copy(id = EventId.random)
      await(repo.createEntity(anotherTeamMemberAddedModel))
      val alreadyNotifiedTeamMemberAddedModel = teamMemberAddedModel.copy(id = EventId.random)
      await(repo.createEntity(alreadyNotifiedTeamMemberAddedModel))
      await(notificationsRepo.createEntity(Notification(alreadyNotifiedTeamMemberAddedModel.id, now(UTC), SENT)))

      val result: Seq[ApplicationEvent] = await(repo.fetchEventsToNotify(TEAM_MEMBER_ADDED).runWith(Sink.seq))

      result should contain only (teamMemberAddedModel, anotherTeamMemberAddedModel)
    }
  }
}
