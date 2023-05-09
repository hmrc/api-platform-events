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

package uk.gov.hmrc.apiplatformevents.models

import play.api.libs.json._
import uk.gov.hmrc.apiplatform.common.domain.services.JsonFormattersSpec
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventTags

import uk.gov.hmrc.apiplatformevents.models.QueryableValues

class QueryableValuesSpec extends JsonFormattersSpec {

  "QueryableValues" when {
    "with a single tag" should {
      val qv = QueryableValues(List(EventTags.SUBSCRIPTION))

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags" -> JsArray(Seq(JsString("SUBSCRIPTION")))
        )
      }

      "read from json" in {
        testFromJson[QueryableValues]("""{"eventTags":["SUBSCRIPTION"]}""")(qv)
      }

      "write as json" in {
        Json.asciiStringify(Json.toJson(qv)) shouldBe """{"eventTags":["SUBSCRIPTION"]}"""
      }
    }

    "with a tags" should {
      val qv = QueryableValues(List(EventTags.SUBSCRIPTION, EventTags.APP_NAME))

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags" -> JsArray(Seq(JsString("SUBSCRIPTION"), JsString("APP_NAME")))
        )
      }

      "read from json" in {
        testFromJson[QueryableValues]("""{"eventTags":["SUBSCRIPTION","APP_NAME"]}""")(qv)
      }

      "write as json" in {
        Json.asciiStringify(Json.toJson(qv)) shouldBe """{"eventTags":["SUBSCRIPTION","APP_NAME"]}"""
      }
    }
  }
}
