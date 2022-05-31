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

package uk.gov.hmrc.apiplatformevents.wiring

import com.google.inject.ImplementedBy
import play.api.Configuration

import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {
  val appName: String
  val thirdPartyApplicationUrl: String
  val emailUrl: String
  def mongoLockTimeoutDuration(job: Option[String] = None): duration.Duration
}

class AppConfigImpl @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  def mongoLockTimeoutDuration(job: Option[String] = None): duration.Duration = {
    val jobName = if (job.isDefined) s"${job.get}." else ""
    duration.Duration(config.get[String](s"schedules.${jobName}mongoLockTimeout"))
  }
  val appName: String = servicesConfig.getString("appName")
  val thirdPartyApplicationUrl: String = servicesConfig.baseUrl("third-party-application")
  val emailUrl: String = servicesConfig.baseUrl("email")
}
