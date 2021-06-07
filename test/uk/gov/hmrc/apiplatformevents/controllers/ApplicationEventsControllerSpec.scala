/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.EventId
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

class ApplicationEventsControllerSpec extends AsyncHmrcSpec with StubControllerComponentsFactory with StubPlayBodyParsersFactory
  with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockApplicationsEventService: ApplicationEventsService = mock[ApplicationEventsService]

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[ApplicationEventsService].to(mockApplicationsEventService))
    .build()

  override def beforeEach(): Unit = {
    reset(mockApplicationsEventService)
  }


  private val teamMemberAddedUri = "/application-events/teamMemberAdded"
  private val teamMemberRemovedUri = "/application-events/teamMemberRemoved"
  private val clientSecretAddedUri = "/application-events/clientSecretAdded"
  private val clientSecretRemovedUri = "/application-events/clientSecretRemoved"
  private val redirectUrisUpdatedUri = "/application-events/redirectUrisUpdated"
  private val apiSubscribedUri = "/application-events/apiSubscribed"
  private val apiUnsubscribedUri = "/application-events/apiUnsubscribed"
  private val ppnsCallBackUriUpdateddUri = "/application-events/ppnsCallbackUriUpdated"
  private val validHeaders: Map[String, String] = Map("Content-Type" -> "application/json")

  "TeamMemberAddedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"teamMemberEmail": "bob@bob.com",
           |"teamMemberRole": "ADMIN"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[TeamMemberAddedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(teamMemberAddedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED

    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[TeamMemberAddedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(teamMemberAddedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR

    }

    "return 400 when post request is invalid json" in {
      val result = doPost(teamMemberAddedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {
      val result = doPost(teamMemberAddedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(teamMemberAddedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }

  "TeamMemberRemovedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"teamMemberEmail": "bob@bob.com",
           |"teamMemberRole": "ADMIN"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[TeamMemberRemovedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(teamMemberRemovedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED

    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[TeamMemberRemovedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(teamMemberRemovedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR

    }

    "return 400 when post request is invalid json" in {
      val result = doPost(teamMemberRemovedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {

      val result = doPost(teamMemberRemovedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(teamMemberRemovedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }

  "ClientSecretAddedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"clientSecretId": "abababab"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[ClientSecretAddedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(clientSecretAddedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[ClientSecretAddedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(clientSecretAddedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(clientSecretAddedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {
      val result = doPost(clientSecretAddedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(clientSecretAddedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }

  "ClientSecretRemovedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"clientSecretId": "abababab"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[ClientSecretRemovedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(clientSecretRemovedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[ClientSecretRemovedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(clientSecretRemovedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(clientSecretRemovedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {

      val result = doPost(clientSecretRemovedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(clientSecretRemovedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }

  "RedirectUrisUpdatedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"oldRedirectUris": "oldrdu",
           |"newRedirectUris": "newrdu"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[RedirectUrisUpdatedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(redirectUrisUpdatedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[RedirectUrisUpdatedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(redirectUrisUpdatedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(redirectUrisUpdatedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {

      val result = doPost(redirectUrisUpdatedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(redirectUrisUpdatedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }

  "ApiSubscribedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"context": "apicontext",
           |"version": "1.0"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[ApiSubscribedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(apiSubscribedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[ApiSubscribedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(apiSubscribedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(apiSubscribedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {

      val result = doPost(apiSubscribedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(apiSubscribedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }

  "ApiUnsubscribedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
           |"eventDateTime": "2014-01-01T13:13:34.441Z",
           |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
           |"context": "apicontext",
           |"version": "1.0"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(*[ApiUnsubscribedEvent]))
        .thenReturn(Future.successful(true))

      val result = doPost(apiUnsubscribedUri, validHeaders, jsonBody)
      status(result) shouldBe CREATED
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureEvent(*[ApiUnsubscribedEvent]))
        .thenReturn(Future.successful(false))

      val result = doPost(apiUnsubscribedUri, validHeaders, jsonBody)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(apiUnsubscribedUri, validHeaders, "Not JSON")
      status(result) shouldBe BAD_REQUEST
    }

    "return 422 when content type header is missing" in {

      val result = doPost(apiUnsubscribedUri, Map.empty, "{}")
      status(result) shouldBe UNPROCESSABLE_ENTITY
    }

    "return 415 when content type isn't json" in {
      val result = doPost(apiUnsubscribedUri, Map("Content-Type" -> "application/xml"), "{}")
      status(result) shouldBe UNSUPPORTED_MEDIA_TYPE
    }
  }


  "PpnsCallBackUriUpdatedEvent" should {

    val jsonBody =
      raw"""{"id": "${EventId.random.value}",
           |"applicationId": "akjhjkhjshjkhksaih",
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

  def doPost(uri: String, headers: Map[String, String], bodyValue: String): Future[Result] = {
    val maybeBody: Option[JsValue] = Try {
      Json.parse(bodyValue)
    } match {
      case Success(value) => Some(value)
      case Failure(_) => None
    }

    val fakeRequest = FakeRequest(POST, uri).withHeaders(headers.toSeq: _*)
    maybeBody.fold(route(app, fakeRequest.withBody(bodyValue)).get)(jsonBody => route(app, fakeRequest.withJsonBody(jsonBody)).get)
  }

}
