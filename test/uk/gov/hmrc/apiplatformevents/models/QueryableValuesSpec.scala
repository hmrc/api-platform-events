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
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ActorType
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.EventTags

import uk.gov.hmrc.apiplatformevents.models.QueryableValues

class QueryableValuesSpec extends JsonFormattersSpec {

  "QueryableValues" when {
    "with a single tag" should {
      val qv   = QueryableValues(List(EventTags.SUBSCRIPTION), List.empty)
      val json = """{"eventTags":[{"description":"API subscription","type":"SUBSCRIPTION"}],"actorTypes":[]}"""

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags"  -> JsArray(
            Seq(
              Json.obj("description" -> JsString("API subscription"), "type" -> JsString("SUBSCRIPTION"))
            )
          ),
          "actorTypes" -> JsArray()
        )
      }

      "read from json" in {
        testFromJson[QueryableValues](json)(qv)
      }

      "write as json" in {
        Json.asciiStringify(Json.toJson(qv)) shouldBe json
      }
    }

    "with a tags" should {
      val qv   = QueryableValues(List(EventTags.SUBSCRIPTION, EventTags.APP_NAME), List.empty)
      val json = """{"eventTags":[{"description":"API subscription","type":"SUBSCRIPTION"},{"description":"Application name","type":"APP_NAME"}],"actorTypes":[]}"""

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags"  -> JsArray(
            Seq(
              Json.obj("description" -> JsString("API subscription"), "type" -> JsString("SUBSCRIPTION")),
              Json.obj("description" -> JsString("Application name"), "type" -> JsString("APP_NAME"))
            )
          ),
          "actorTypes" -> JsArray()
        )
      }

      "read from json" in {
        testFromJson[QueryableValues](json)(qv)
      }

      "write as json" in {
        Json.asciiStringify(Json.toJson(qv)) shouldBe json
      }
    }

    "with a single actorType" should {
      val qv   = QueryableValues(List.empty, List(ActorType.GATEKEEPER))
      val json = """{"eventTags":[],"actorTypes":[{"description":"Gatekeeper User","type":"GATEKEEPER"}]}"""

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags"  -> JsArray(),
          "actorTypes" -> JsArray(Seq(Json.obj("description" -> JsString("Gatekeeper User"), "type" -> JsString("GATEKEEPER"))))
        )
      }

      "read from json" in {
        testFromJson[QueryableValues](json)(qv)
      }

      "write as json" in {
        Json.asciiStringify(Json.toJson(qv)) shouldBe json
      }
    }

    "with both" should {
      val qv   = QueryableValues(List(EventTags.SUBSCRIPTION), List(ActorType.GATEKEEPER))
      val json =
        """{"eventTags":[{"description":"API subscription","type":"SUBSCRIPTION"}],"actorTypes":[{"description":"Gatekeeper User","type":"GATEKEEPER"}]}""".stripMargin

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags"  -> JsArray(Seq(Json.obj("description" -> JsString("API subscription"), "type" -> JsString("SUBSCRIPTION")))),
          "actorTypes" -> JsArray(Seq(Json.obj("description" -> JsString("Gatekeeper User"), "type" -> JsString("GATEKEEPER"))))
        )
      }

      "read from json" in {
        testFromJson[QueryableValues](json)(qv)
      }

      "write as json" in {
        Json.asciiStringify(
          Json.toJson(qv)
        ) shouldBe json
      }
    }

    "with multiple actorTypes" should {
      val qv   = QueryableValues(List.empty, List(ActorType.COLLABORATOR, ActorType.GATEKEEPER))
      val json = """{"eventTags":[],"actorTypes":[{"description":"Application Collaborator","type":"COLLABORATOR"},{"description":"Gatekeeper User","type":"GATEKEEPER"}]}"""

      "convert to json" in {
        Json.toJson(qv) shouldBe Json.obj(
          "eventTags"  -> JsArray(),
          "actorTypes" -> JsArray(
            Seq(
              Json.obj("type" -> JsString("COLLABORATOR"), "description" -> JsString("Application Collaborator")),
              Json.obj("type" -> JsString("GATEKEEPER"), "description"   -> JsString("Gatekeeper User"))
            )
          )
        )
      }

      "read from json" in {
        testFromJson[QueryableValues](json)(qv)
      }

      "write as json" in {
        Json.asciiStringify(Json.toJson(qv)) shouldBe json
      }
    }

  }
}
