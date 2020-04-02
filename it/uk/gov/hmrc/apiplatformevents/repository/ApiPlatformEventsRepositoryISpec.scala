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

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.models.db.{ApiPlatformApplicationEvent, ApplicationEventTypeEnumeration}
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

  def repo: ApplicationEventsRepository =
    app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach() {
    super.beforeEach()
    await(repo.ensureIndexes)
  }

  val model = TeamMemberAddedEvent(applicationId = "John Smith",
    eventTimeStamp = 1234558L,
    teamMemberEmail = "jkhkhk",
    teamMemberRole = "ADMIN")

  "createEntity" should {
    "create an entity" in {
      await(repo.createEntity(model))
      await(repo.find())
    }

  }
}
