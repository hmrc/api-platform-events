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

package uk.gov.hmrc.apiplatformevents.controllers.ppns

import java.util as ju
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider

import play.api.libs.ws.WSBodyReadables.readableAsString
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.*
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.*
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.ApplicationEvents.*
import uk.gov.hmrc.mongo.logging.ObservableFutureImplicits.ObservableFuture

import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

class PpnsEventsControllerISpec extends ServerBaseISpec with AuditService with BeforeAndAfterEach {

  this: Suite & ServerProvider =>

  def repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    primeAuditService()
    await(repo.collection.drop().toFuture())
  }

  val url = s"http://localhost:$port"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val eventId: UUID              = EventId.random.value
  val applicationId              = ApplicationId.random
  val appIdText                  = applicationId.value.toString()
  val clientId                   = ClientId.random
  val clientIdText               = clientId.value
  val submissionId: String       = ju.UUID.randomUUID.toString
  val actorId                    = "123454654"
  val actorEmail                 = "actor@example.com"
  val actorTypeGK                = "GATEKEEPER"
  val actorTypeCollab            = "COLLABORATOR"
  val actorUser                  = "gatekeeper"
  val inputInstantString         = "2014-01-01T13:13:34.441"
  val expectedEventInstantString = s"${inputInstantString}Z"

  def validPpnsCallBackUpdatedJsonBody(boxId: String, boxName: String, oldCallbackUrl: String, newCallbackUrl: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"boxId": "$boxId",
         |"boxName": "$boxName",
         |"oldCallbackUrl": "$oldCallbackUrl",
         |"newCallbackUrl": "$newCallbackUrl"}""".stripMargin

  def doGet(path: String): Future[WSResponse] = {
    wsClient
      .url(s"$url$path")
      .get()
  }

  def doPost(path: String, body: String, headers: (String, String)): Future[WSResponse] = {
    import play.api.libs.ws.WSBodyWritables.writeableOf_String
    wsClient
      .url(s"$url$path")
      .withHttpHeaders(headers)
      .post(body)
  }

  def checkCommonEventValues(event: ApplicationEvent): Unit = {
    event.applicationId shouldBe applicationId
    event.eventDateTime.toString shouldBe expectedEventInstantString
  }

  "PpnsEventsController" when {

    "POST /ppnsCallbackUriUpdated" should {
      "respond with 201 when valid json is sent" in {
        val boxId          = ju.UUID.randomUUID().toString
        val boxName        = "some##box##name"
        val oldCallbackUrl = "https://foo.bar/baz"
        val newCallbackUrl = "https://foo.bar/bazbazbaz"

        testSuccessScenario("/application-events/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody(boxId, boxName, oldCallbackUrl, newCallbackUrl))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1L
        val event   = results.head.asInstanceOf[PpnsCallBackUriUpdatedEvent]

        checkCommonEventValues(event)
        event.boxId shouldBe boxId
        event.oldCallbackUrl shouldBe oldCallbackUrl
        event.newCallbackUrl shouldBe newCallbackUrl
        event.boxName shouldBe boxName
        event.actor shouldBe Actors.GatekeeperUser(actorId)
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/ppnsCallbackUriUpdated")
      }

    }

    def testSuccessScenario(uriToTest: String, bodyString: String): Unit = {
      val result = await(doPost(uriToTest, bodyString, "Content-Type" -> "application/json"))
      result.status shouldBe 201
      result.body shouldBe ""
    }

    def testErrorScenarios(uriToTest: String): Unit = {
      val result2 = await(doPost(uriToTest, "{\"SomeJson\": \"hello\"}", "somHeader" -> "someValue"))
      withClue("should respond with 415 when contentType header is missing") {
        result2.status shouldBe 415
        result2.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }

      val result3 = await(doPost(uriToTest, "{\"SomeJson\": \"hello\"}", "Content-Type" -> "application/xml"))
      withClue("should respond with 415 when contentType header isn't JSON") {
        result3.status shouldBe 415
        result3.body shouldBe "{\"statusCode\":415,\"message\":\"Expecting text/json or application/json body\"}"
      }
    }
  }
}
