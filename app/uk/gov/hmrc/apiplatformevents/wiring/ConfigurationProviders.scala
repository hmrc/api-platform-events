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

package uk.gov.hmrc.apiplatformevents.wiring

import java.util.concurrent.TimeUnit.{MINUTES, SECONDS}

import javax.inject.{Inject, Provider, Singleton}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apiplatformevents.scheduled.SendEventNotificationsJobConfig

import scala.concurrent.duration.{Duration, FiniteDuration}

class ConfigurationModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[SendEventNotificationsJobConfig].toProvider[SendEventNotificationsJobConfigProvider]
    )
  }
}

@Singleton
class SendEventNotificationsJobConfigProvider  @Inject()(configuration: Configuration)
  extends Provider[SendEventNotificationsJobConfig] {

  override def get(): SendEventNotificationsJobConfig = {
    // scalastyle:off magic.number
    val initialDelay = configuration.getOptional[String]("sendEventNotificationsJob.initialDelay").map(Duration.create(_).asInstanceOf[FiniteDuration])
      .getOrElse(FiniteDuration(60, SECONDS))
    val interval = configuration.getOptional[String]("sendEventNotificationsJob.interval").map(Duration.create(_).asInstanceOf[FiniteDuration])
      .getOrElse(FiniteDuration(5, MINUTES))
    val enabled = configuration.getOptional[Boolean]("sendEventNotificationsJob.enabled").getOrElse(false)
    val parallelism = configuration.getOptional[Int]("sendEventNotificationsJob.parallelism").getOrElse(10)
    SendEventNotificationsJobConfig(initialDelay, interval, enabled, parallelism)
  }
}
