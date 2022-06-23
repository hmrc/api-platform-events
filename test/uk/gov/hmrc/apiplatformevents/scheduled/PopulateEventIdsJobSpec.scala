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

package uk.gov.hmrc.apiplatformevents.scheduled

/*
 * Copyright 2020 HM Revenue & Customs
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

import org.joda.time.Duration
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.utils.{AsyncHmrcSpec, HmrcSpec}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lock.LockRepository
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration.{FiniteDuration, HOURS, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

class PopulateEventIdsJobSpec extends AsyncHmrcSpec  with MongoSpecSupport {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val lockKeeperSuccess: () => Boolean = () => true
    private val reactiveMongoComponent = new ReactiveMongoComponent {
      override def mongoConnector: MongoConnector = mongoConnectorForTest
    }
    val populateEventIdsJobConfig: PopulateEventIdsJobConfig = PopulateEventIdsJobConfig(FiniteDuration(60, SECONDS), FiniteDuration(24, HOURS), enabled = true, 1)
    val mockLockKeeper: PopulateEventIdsJobLockKeeper = new PopulateEventIdsJobLockKeeper(reactiveMongoComponent) {
      override def lockId: String = "testLock"
      override def repo: LockRepository = mock[LockRepository]
      override val forceLockReleaseAfter: Duration = Duration.standardMinutes(5) // scalastyle:off magic.number
      override def tryLock[T](body: => Future[T])(implicit ec: ExecutionContext): Future[Option[T]] =
        if (lockKeeperSuccess()) body.map(Some(_))
        else successful(None)
    }

    val mockApplicationEventsRepository: ApplicationEventsRepository = mock[ApplicationEventsRepository]
    val underTest = new PopulateEventIdsJob(
      mockLockKeeper,
      populateEventIdsJobConfig,
      mockApplicationEventsRepository
    )
  }

  "PopulateEventIdsJobSpec" should {
    import scala.concurrent.ExecutionContext.Implicits.global

    "populate missing event IDs" in new Setup {
      when(mockApplicationEventsRepository.populateEventIds()).thenReturn(successful(()))
      val result: underTest.Result = await(underTest.execute)

      verify(mockApplicationEventsRepository, times(1)).populateEventIds()
      result.message shouldBe "PopulateEventIdsJob Job ran successfully."
    }

    "not execute if the job is already running" in new Setup {
      override val lockKeeperSuccess: () => Boolean = () => false

      val result: underTest.Result = await(underTest.execute)

      verify(mockApplicationEventsRepository, never).populateEventIds()
      result.message shouldBe "PopulateEventIdsJob did not run because repository was locked by another instance of the scheduler."
    }

    "handle error when something fails" in new Setup {
      when(mockApplicationEventsRepository.populateEventIds()).thenReturn(failed(new RuntimeException("Failed")))

      val result: underTest.Result = await(underTest.execute)

      result.message shouldBe "The execution of scheduled job PopulateEventIdsJob failed with error 'Failed'. " +
        "The next execution of the job will do retry."
    }
  }
}
