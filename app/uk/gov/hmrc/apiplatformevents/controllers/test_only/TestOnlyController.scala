/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiplatformevents.controllers.test_only

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.mvc.*
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.apiplatformevents.util.ApplicationLogger

@Singleton
class TestOnlyController @Inject() (
    val env: Environment,
    service: ApplicationEventsService,
    cc: ControllerComponents
)(implicit
    val configuration: Configuration,
    ec: ExecutionContext
) extends BackendController(cc)
    with ApplicationLogger {

  // Note that this a test-only route, for use in QA only
  def deleteEventsForApplication(rawApplicationId: UUID): Action[AnyContent] = Action.async { _ =>
    val applicationId                         = ApplicationId(rawApplicationId)
    def success(numberOfRecordsDeleted: Long) = {
      logger.info(s"test-only: Successfully deleted $numberOfRecordsDeleted event records for application $applicationId")
      NoContent
    }
    service.deleteEventsForApplication(applicationId).map(success)
  }
}
