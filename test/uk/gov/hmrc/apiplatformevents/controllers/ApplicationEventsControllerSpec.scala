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

package uk.gov.hmrc.apiplatformevents.controllers

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.mockito.Mockito.verifyNoInteractions
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory, StubPlayBodyParsersFactory}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.ApplicationEvents._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._

import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

class ApplicationEventsControllerSpec extends AsyncHmrcSpec with StubControllerComponentsFactory with StubPlayBodyParsersFactory with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockApplicationsEventService: ApplicationEventsService = mock[ApplicationEventsService]

  val appId     = ApplicationId.random
  val appIdText = appId.value.toString()

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "application.router" -> "testOnlyDoNotUseInAppConf.Routes"
    )
    .overrides(bind[ApplicationEventsService].to(mockApplicationsEventService))
    .build()

  override def beforeEach(): Unit = {
    reset(mockApplicationsEventService)
  }

  private val ppnsCallBackUriUpdateddUri = "/application-events/ppnsCallbackUriUpdated"
  private val handleEventUri             = "/application-event"

  private val validHeaders: Map[String, String] = Map("Content-Type" -> "application/json")

  "PpnsCallBackUriUpdatedEvent" should {
    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "$appIdText",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"boxId": "boxId",
           |"boxName": "some##box##name",
           |"oldCallbackUrl": "oldUri",
           |"newCallbackUrl": "newUri"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[PpnsCallBackUriUpdatedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(ppnsCallBackUriUpdateddUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[PpnsCallBackUriUpdatedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(ppnsCallBackUriUpdateddUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(ppnsCallBackUriUpdateddUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
      verifyNoInteractions(mockApplicationsEventService)
    }

    "return 422 when content type header is missing" in {
      val result = doPost(ppnsCallBackUriUpdateddUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
      verifyNoInteractions(mockApplicationsEventService)
    }

    "return 415 when content type isn't json" in {
      val result = doPost(ppnsCallBackUriUpdateddUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
      verifyNoInteractions(mockApplicationsEventService)
    }
  }

  "ProductionAppNameChangedEvent" should {
    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "$appIdText",
           |"eventType": "PROD_APP_NAME_CHANGED",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"user": "gk user", "actorType": "GATEKEEPER"},
           |"oldAppName": "oldAppName",
           |"newAppName": "newAppName",
           |"requestingAdminEmail": "admin@example.com"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[ProductionAppNameChangedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(handleEventUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[ProductionAppNameChangedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(handleEventUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 when post request is valid json but service throws" in {
      when(mockApplicationsEventService.captureEvent(*[ProductionAppNameChangedEvent]))
        .thenReturn(Future.failed(new RuntimeException("Bang")))

      val result = doPost(handleEventUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(handleEventUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
      verifyNoInteractions(mockApplicationsEventService)
    }

    "return 422 when content type header is missing" in {
      val result = doPost(handleEventUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
      verifyNoInteractions(mockApplicationsEventService)
    }

    "return 415 when content type isn't json" in {
      val result = doPost(handleEventUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
      verifyNoInteractions(mockApplicationsEventService)
    }
  }

  "DeleteEventsForApplication" should {
    val appId          = ApplicationId.random
    val deleteEventUri = s"/test-only/application-event/${appId}/delete"

    "return 204 when post request" in {

      when(mockApplicationsEventService.deleteEventsForApplication(eqTo(appId)))
        .thenReturn(Future.successful(1))

      val result = doPost(deleteEventUri, validHeaders)
      status(result) shouldBe NO_CONTENT
    }

    "return 400 when application id is invalid" in {
      val deleteEventUri = "/test-only/application-event/invalid-app-id/delete"

      val result = doPost(deleteEventUri, validHeaders)
      status(result) shouldBe BAD_REQUEST
    }
  }

  def doPost(uri: String, headers: Map[String, String]): Future[Result] = {
    val fakeRequest = FakeRequest(POST, uri).withHeaders(headers.toSeq: _*)
    route(app, fakeRequest).get
  }

  def doPost(uri: String, headers: Map[String, String], bodyValue: String): Future[Result] = {
    val maybeBody: Option[JsValue] = Try {
      Json.parse(bodyValue)
    } match {
      case Success(value) => Some(value)
      case Failure(_)     => None
    }

    val fakeRequest = FakeRequest(POST, uri).withHeaders(headers.toSeq: _*)
    maybeBody.fold(route(app, fakeRequest.withBody(bodyValue)).get)(jsonBody => route(app, fakeRequest.withJsonBody(jsonBody)).get)
  }
}
