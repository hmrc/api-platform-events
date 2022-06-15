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
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.SENT
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.EventId
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class NotificationsRepositoryISpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with DefaultPlayMongoRepositorySupport[Notification] {

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  override implicit lazy val app: Application = appBuilder.build()

  protected def repository: PlayMongoRepository[Notification]= new NotificationsRepository(mongoComponent)
  val repo: NotificationsRepository = repository.asInstanceOf[NotificationsRepository]

  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }


  "createEntity" should {
    "create an entity" in {
      val notification = Notification(EventId.random, LocalDateTime.now, SENT)

      await(repo.createEntity(notification))

      await(repo.collection.find().toFuture()) should contain only notification
    }
  }
}
