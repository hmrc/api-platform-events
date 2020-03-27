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

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {

  val appName: String

  val someInt: Int
  val someBoolean: Boolean

  val authBaseUrl: String
  val fooBaseUrl: String
}

class AppConfigImpl @Inject()(config: ServicesConfig) extends AppConfig {
  val appName: String = config.getString("appName")

  val someInt: Int = config.getInt("someInt")
  val someBoolean: Boolean = config.getBoolean("someBoolean")

  val authBaseUrl: String = config.baseUrl("auth")
  val fooBaseUrl: String = config.baseUrl("foo")
}
