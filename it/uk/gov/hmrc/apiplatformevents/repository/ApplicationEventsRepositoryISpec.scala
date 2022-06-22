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
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.apiplatformevents.data.ApplicationEventTestData

class ApplicationEventsRepositoryISpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ApplicationEventTestData {

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  val repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach() {
    await(repo.collection.drop().toFuture())
    await(repo.ensureIndexes)
  }

  "createEntity" should {
    "create a productionAppNameChangedEvent entity" in {
      await(repo.createEntity(productionAppNameChangedEvent))
      await(repo.collection.find().toFuture()) should contain only productionAppNameChangedEvent
    }
  }
}
