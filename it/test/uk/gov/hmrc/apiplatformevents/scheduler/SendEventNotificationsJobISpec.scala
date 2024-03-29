/*
 * Copyright 2024 HM Revenue & Customs
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

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData
import uk.gov.hmrc.apiplatformevents.models.{Notification, NotificationStatus}
import uk.gov.hmrc.apiplatformevents.repository.{ApplicationEventsRepository, NotificationsRepository}
import uk.gov.hmrc.apiplatformevents.scheduler.jobs.SendEventNotificationsService
import uk.gov.hmrc.apiplatformevents.support.{EmailService, MongoHelpers, ServerBaseISpec, ThirdPartyApplicationService}

class SendEventNotificationsJobISpec extends ServerBaseISpec with MongoHelpers with ThirdPartyApplicationService with EmailService with ApplicationEventTestData with FixedClock {

  override protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri"                                        -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}",
        "schedules.SendEventNotificationsJob.enabled"        -> false,
        "microservice.services.third-party-application.host" -> wireMockHost,
        "microservice.services.third-party-application.port" -> wireMockPort,
        "microservice.services.email.host"                   -> wireMockHost,
        "microservice.services.email.port"                   -> wireMockPort
      )

  class Setup {
    val sendNotificationsJob: SendEventNotificationsService      = app.injector.instanceOf[SendEventNotificationsService]
    val notificationsRepository: NotificationsRepository         = app.injector.instanceOf[NotificationsRepository]
    val applicationEventsRepository: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]
    val lockRepo: MongoLockRepository                            = app.injector.instanceOf[MongoLockRepository]
    removeAll(applicationEventsRepository)
    count(applicationEventsRepository) shouldBe 0
    removeAll(notificationsRepository)
    count(notificationsRepository) shouldBe 0

    removeAll(lockRepo)
  }

  "invoke" must {

    "return right true and create notification when event of correct type exist but has not been notified yet" in new Setup {
      count(notificationsRepository) shouldBe 0
      primeApplicationEndpoint(200, Json.toJson(appResponseWithAdmins).toString(), ppnsCallBackUriUpdatedEvent.applicationId)
      primeEmailEndpoint(200)

      insert(applicationEventsRepository, teamMemberAddedModel)
      insert(applicationEventsRepository, teamMemberRemovedModel)
      insert(applicationEventsRepository, clientSecretAddedModel)
      insert(applicationEventsRepository, clientSecretRemovedModel)
      insert(applicationEventsRepository, ppnsCallBackUriUpdatedEvent)

      val result = await(sendNotificationsJob.invoke)
      result match {
        case Right(innerObj) =>
          innerObj shouldBe true
          count(notificationsRepository) shouldBe 1
        case _               => fail()
      }
    }

    "return right true and create notification when ppns event exists but application Not Found" in new Setup {
      count(notificationsRepository) shouldBe 0
      primeApplicationEndpoint(404, "", ppnsCallBackUriUpdatedEvent.applicationId)

      insert(applicationEventsRepository, teamMemberAddedModel)
      insert(applicationEventsRepository, teamMemberRemovedModel)
      insert(applicationEventsRepository, clientSecretAddedModel)
      insert(applicationEventsRepository, clientSecretRemovedModel)
      insert(applicationEventsRepository, ppnsCallBackUriUpdatedEvent)

      val result = await(sendNotificationsJob.invoke)
      result match {
        case Right(innerObj) =>
          innerObj shouldBe true
          count(notificationsRepository) shouldBe 1
        case _               => fail()
      }
    }

    "return right true when no events exist" in new Setup {
      count(applicationEventsRepository) shouldBe 0

      val result = await(sendNotificationsJob.invoke)
      result match {
        case Right(innerObj) => innerObj shouldBe true
        case _               => fail()
      }
    }

    "return right true when no events of correct type exist" in new Setup {
      insert(applicationEventsRepository, teamMemberAddedModel)
      insert(applicationEventsRepository, teamMemberRemovedModel)
      insert(applicationEventsRepository, clientSecretAddedModel)
      insert(applicationEventsRepository, clientSecretRemovedModel)
      count(notificationsRepository) shouldBe 0

      val result = await(sendNotificationsJob.invoke)
      result match {
        case Right(innerObj) => innerObj shouldBe true
        case _               => fail()
      }
    }

    "return right true and create notification when event of correct type exist but has already been notified" in new Setup {
      count(notificationsRepository) shouldBe 0
      insert(notificationsRepository, Notification(ppnsCallBackUriUpdatedEvent.id, instant, NotificationStatus.SENT))

      count(notificationsRepository) shouldBe 1
      insert(applicationEventsRepository, teamMemberAddedModel)
      insert(applicationEventsRepository, teamMemberRemovedModel)
      insert(applicationEventsRepository, clientSecretAddedModel)
      insert(applicationEventsRepository, clientSecretRemovedModel)
      insert(applicationEventsRepository, ppnsCallBackUriUpdatedEvent)

      val result = await(sendNotificationsJob.invoke)
      result match {
        case Right(innerObj) =>
          innerObj shouldBe true
          count(notificationsRepository) shouldBe 1
        case _               => fail()
      }
    }
  }

}
