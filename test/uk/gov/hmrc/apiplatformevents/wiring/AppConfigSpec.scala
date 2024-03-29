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

package uk.gov.hmrc.apiplatformevents.wiring

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

class AppConfigSpec extends AsyncHmrcSpec {

  private val mockServiceConfig = mock[ServicesConfig]
  private val mockConfiguration = mock[Configuration]
  private val appName           = "TestAppName"

  trait Setup {
    when(mockServiceConfig.getString("appName")).thenReturn(appName)
    val objInTest = new AppConfigImpl(mockConfiguration, mockServiceConfig)
  }

  "appconfig" should {

    "returns value from service config when called" in new Setup {
      objInTest.appName shouldBe appName
    }
  }

}
