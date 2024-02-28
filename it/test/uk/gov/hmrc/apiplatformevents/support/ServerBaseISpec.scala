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

package uk.gov.hmrc.apiplatformevents.support

import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

abstract class ServerBaseISpec extends BaseISpec with GuiceOneServerPerSuite with TestApplication {

  override implicit lazy val app: Application = appBuilder.build()

  override protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri"                                 -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}",
        "schedules.SendEventNotificationsJob.enabled" -> false,
        "services.third-party-application.host"       -> wireMockHost,
        "services.third-party-application.port"       -> wireMockPort,
        "services.email.host"                         -> wireMockHost,
        "services.email.port"                         -> wireMockPort
      )
}
