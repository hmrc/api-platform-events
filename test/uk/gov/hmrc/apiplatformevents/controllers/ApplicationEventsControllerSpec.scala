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
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory, StubPlayBodyParsersFactory}
import uk.gov.hmrc.apiplatformevents.models.TeamMemberAddedEvent
import uk.gov.hmrc.apiplatformevents.services.ApplicationEventsService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ApplicationEventsControllerSpec extends UnitSpec with StubControllerComponentsFactory with StubPlayBodyParsersFactory with MockitoSugar
with GuiceOneAppPerSuite{

  val mockApplicationsEventService = mock[ApplicationEventsService]

  override lazy val app = GuiceApplicationBuilder()
    .overrides(bind[ApplicationEventsService].to(mockApplicationsEventService))
    .build()



  private val teamMemberAddedUri = "/api-platform-events/application-events/teamMemberAdded"
  private val validHeaders: Map[String, String] = Map("Content-Type"->"application/json")

  "ApplicationEventsController" should {

    "return 201 when post request is valid json" in {
      when(mockApplicationsEventService.captureEvent(any[TeamMemberAddedEvent])(any(), any()))
        .thenReturn(Future.successful(true))

      val result = doPost(teamMemberAddedUri, validHeaders, "{\n\t\"applicationId\": \"akjhjkhjshjkhksaih\",\n\t\"eventTimeStamp\": 1585830790,\n\t\"teamMemberEmail\": \"bob@bob.com\",\n\t\"teamMemberRole\": \"ADMIN\"\n\n}")
      status(result) should be(CREATED)
    }

    "return 400 when post request is invalid json" in {
      val result = doPost(teamMemberAddedUri, validHeaders, "Not JSON")
      status(result) should be(BAD_REQUEST)
    }

//    "return 400 when content type header is missing" in {
//      // TODO: This should fail until header validation is added
//      val result = doPost(teamMemberAddedUri, Map.empty, "{}")
//      status(result) should be(BAD_REQUEST)
//    }

    "return 400 when content type isn't json" in {
      val result = doPost(teamMemberAddedUri, Map("Content-Type"-> "application/xml"), "{}")
      status(result) should be(BAD_REQUEST)
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
