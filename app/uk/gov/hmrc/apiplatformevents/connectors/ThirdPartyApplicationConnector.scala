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

package uk.gov.hmrc.apiplatformevents.connectors

import com.google.inject.Inject
import javax.inject.Singleton
import uk.gov.hmrc.apiplatformevents.models.ApplicationResponse
import uk.gov.hmrc.apiplatformevents.wiring.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.apiplatform.modules.applications.domain.models.ApplicationId

@Singleton
class ThirdPartyApplicationConnector @Inject()(http: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def getApplication(applicationId: ApplicationId)(implicit hc: HeaderCarrier): Future[ApplicationResponse] = {
    http.GET[ApplicationResponse](s"${appConfig.thirdPartyApplicationUrl}/application/${applicationId.value.toString()}")
  }
}
