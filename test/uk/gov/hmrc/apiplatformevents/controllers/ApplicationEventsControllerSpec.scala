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

package uk.gov.hmrc.apiplatformevents.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterAllConfigMap, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory, StubPlayBodyParsersFactory}
import uk.gov.hmrc.apiplatformevents.models.{ClientSecretAddedEvent, ClientSecretRemovedEvent, TeamMemberAddedEvent, TeamMemberRemovedEvent}
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito.reset

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ApplicationEventsControllerSpec extends UnitSpec with StubControllerComponentsFactory with StubPlayBodyParsersFactory with MockitoSugar
with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockApplicationsEventService = mock[ApplicationEventsService]

  override lazy val app = GuiceApplicationBuilder()
    .overrides(bind[ApplicationEventsService].to(mockApplicationsEventService))
    .build()

  override def beforeEach() = {
    reset(mockApplicationsEventService)
  }


  private val teamMemberAddedUri = "/application-events/teamMemberAdded"
  private val teamMemberRemovedUri = "/application-events/teamMemberRemoved"
  private val clientSecretAddedUri = "/application-events/clientSecretAdded"
  private val clientSecretRemovedUri = "/application-events/clientSecretRemoved"
  private val validHeaders: Map[String, String] = Map("Content-Type"->"application/json")

  "TeamMemberAddedEvent" should {

    val jsonBody =  raw"""{"applicationId": "akjhjkhjshjkhksaih",
                         |"eventDateTime": "2014-01-01T13:13:34.441Z",
                         |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
                         |"teamMemberEmail": "bob@bob.com",
                         |"teamMemberRole": "ADMIN"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureTeamMemberAddedEvent(any[TeamMemberAddedEvent])(any(), any()))
        .thenReturn(Future.successful(true))

      val result = await(doPost(teamMemberAddedUri, validHeaders, jsonBody))
      status(result) should be(CREATED)

    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureTeamMemberAddedEvent(any[TeamMemberAddedEvent])(any(), any()))
        .thenReturn(Future.successful(false))

      val result = await(doPost(teamMemberAddedUri, validHeaders, jsonBody))
      status(result) should be(INTERNAL_SERVER_ERROR)

    }

    "return 400 when post request is invalid json" in {
      val result = doPost(teamMemberAddedUri, validHeaders, "Not JSON")
      status(result) should be(BAD_REQUEST)
    }

    "return 422 when content type header is missing" in {

      val result = doPost(teamMemberAddedUri, Map.empty, "{}")
      status(result) should be(UNPROCESSABLE_ENTITY)
    }

    "return 415 when content type isn't json" in {
      val result = doPost(teamMemberAddedUri, Map("Content-Type"-> "application/xml"), "{}")
      status(result) should be(UNSUPPORTED_MEDIA_TYPE)
    }
  }

  "TeamMemberRemovedEvent" should {

    val jsonBody =  raw"""{"applicationId": "akjhjkhjshjkhksaih",
                         |"eventDateTime": "2014-01-01T13:13:34.441Z",
                         |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
                         |"teamMemberEmail": "bob@bob.com",
                         |"teamMemberRole": "ADMIN"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureTeamMemberRemovedEvent(any[TeamMemberRemovedEvent])(any(), any()))
        .thenReturn(Future.successful(true))

      val result = await(doPost(teamMemberRemovedUri, validHeaders, jsonBody))
      status(result) should be(CREATED)

    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureTeamMemberRemovedEvent(any[TeamMemberRemovedEvent])(any(), any()))
        .thenReturn(Future.successful(false))

      val result = await(doPost(teamMemberRemovedUri, validHeaders, jsonBody))
      status(result) should be(INTERNAL_SERVER_ERROR)

    }

    "return 400 when post request is invalid json" in {
      val result = doPost(teamMemberRemovedUri, validHeaders, "Not JSON")
      status(result) should be(BAD_REQUEST)
    }

    "return 422 when content type header is missing" in {

      val result = doPost(teamMemberRemovedUri, Map.empty, "{}")
      status(result) should be(UNPROCESSABLE_ENTITY)
    }

    "return 415 when content type isn't json" in {
      val result = doPost(teamMemberRemovedUri, Map("Content-Type"-> "application/xml"), "{}")
      status(result) should be(UNSUPPORTED_MEDIA_TYPE)
    }
  }

  "ClientSecretAddedEvent" should {

    val jsonBody =  raw"""{"applicationId": "akjhjkhjshjkhksaih",
                         |"eventDateTime": "2014-01-01T13:13:34.441Z",
                         |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
                         |"clientSecretId": "abababab"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureClientSecretAddedEvent(any[ClientSecretAddedEvent])(any(), any()))
        .thenReturn(Future.successful(true))

      val result = await(doPost(clientSecretAddedUri, validHeaders, jsonBody))
      status(result) should be(CREATED)
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureClientSecretAddedEvent(any[ClientSecretAddedEvent])(any(), any()))
        .thenReturn(Future.successful(false))

      val result = await(doPost(clientSecretAddedUri, validHeaders, jsonBody))
      status(result) should be(INTERNAL_SERVER_ERROR)
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(clientSecretAddedUri, validHeaders, "Not JSON")
      status(result) should be(BAD_REQUEST)
    }

    "return 422 when content type header is missing" in {

      val result = doPost(clientSecretAddedUri, Map.empty, "{}")
      status(result) should be(UNPROCESSABLE_ENTITY)
    }

    "return 415 when content type isn't json" in {
      val result = doPost(clientSecretAddedUri, Map("Content-Type"-> "application/xml"), "{}")
      status(result) should be(UNSUPPORTED_MEDIA_TYPE)
    }
  }

  "ClientSecretRemovedEvent" should {

    val jsonBody =  raw"""{"applicationId": "akjhjkhjshjkhksaih",
                         |"eventDateTime": "2014-01-01T13:13:34.441Z",
                         |"actor":{"id": "123454654", "actorType": "GATEKEEPER"},
                         |"clientSecretId": "abababab"}""".stripMargin

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureClientSecretRemovedEvent(any[ClientSecretRemovedEvent])(any(), any()))
        .thenReturn(Future.successful(true))

      val result = await(doPost(clientSecretRemovedUri, validHeaders, jsonBody))
      status(result) should be(CREATED)
    }

    "return 500 when post request is valid json but service fails" in {
      when(mockApplicationsEventService.captureClientSecretRemovedEvent(any[ClientSecretRemovedEvent])(any(), any()))
        .thenReturn(Future.successful(false))

      val result = await(doPost(clientSecretRemovedUri, validHeaders, jsonBody))
      status(result) should be(INTERNAL_SERVER_ERROR)
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(clientSecretRemovedUri, validHeaders, "Not JSON")
      status(result) should be(BAD_REQUEST)
    }

    "return 422 when content type header is missing" in {

      val result = doPost(clientSecretRemovedUri, Map.empty, "{}")
      status(result) should be(UNPROCESSABLE_ENTITY)
    }

    "return 415 when content type isn't json" in {
      val result = doPost(clientSecretRemovedUri, Map("Content-Type"-> "application/xml"), "{}")
      status(result) should be(UNSUPPORTED_MEDIA_TYPE)
    }
  }

  def doPost(uri: String, headers: Map[String, String], bodyValue: String): Future[Result] ={
   val maybeBody: Option[JsValue] =  Try{
      Json.parse(bodyValue)
    } match {
     case Success(value) => Some(value)
     case Failure(_) =>  None
   }

    val fakeRequest =  FakeRequest(POST, uri).withHeaders(headers.toSeq: _*)
    maybeBody
      .fold(route(app, fakeRequest.withBody(bodyValue)).get)(jsonBody => route(app, fakeRequest.withJsonBody(jsonBody)).get)

  }

}
