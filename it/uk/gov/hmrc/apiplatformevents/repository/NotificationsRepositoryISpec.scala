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

import org.joda.time.DateTime.now
import org.joda.time.DateTimeZone.UTC
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.api.indexes.Index
import uk.gov.hmrc.apiplatformevents.models.NotificationStatus.SENT
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.EventId
import uk.gov.hmrc.apiplatformevents.support.MongoApp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationsRepositoryISpec extends UnitSpec with MongoApp {

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  override implicit lazy val app: Application = appBuilder.build()

  def repo: NotificationsRepository = app.injector.instanceOf[NotificationsRepository]

  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }

  def getIndex(indexName: String): Option[Index] ={
    await(repo.collection.indexesManager.list().map(_.find(_.eventualName.equalsIgnoreCase(indexName))))
  }

  "Indexes" should {
    "create unique event ID index"in {
      val Some(index) = getIndex("event_id_index")
      index.unique shouldBe true
    }
  }

  "createEntity" should {
    "create an entity" in {
      val notification = Notification(EventId.random, now(UTC), SENT)

      await(repo.createEntity(notification))

      await(repo.find()) should contain only notification
    }
  }
}
