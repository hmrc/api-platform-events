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

package uk.gov.hmrc.apiplatformevents.controllers

import java.util.UUID
import java.{util => ju}
import scala.concurrent.Future

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider

import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatform.modules.applications.submissions.domain.models._
import uk.gov.hmrc.apiplatform.modules.common.domain.models._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models.ApplicationEvents._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._

import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

class ApplicationEventsControllerISpec extends ServerBaseISpec with AuditService with BeforeAndAfterEach {

  this: Suite with ServerProvider =>

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

  def validClientSecretJsonBody(clientSecretId: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"clientSecretId": "$clientSecretId"}""".stripMargin

  def validRedirectUrisUpdatedJsonBody(oldRedirectUri: String, newRedirectUri: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"oldRedirectUris": "$oldRedirectUri",
         |"newRedirectUris": "$newRedirectUri"}""".stripMargin

  def validApiSubscriptionJsonBody(apiContext: String, apiVersion: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"context": "$apiContext",
         |"version": "$apiVersion"}""".stripMargin

  def validPpnsCallBackUpdatedJsonBody(boxId: String, boxName: String, oldCallbackUrl: String, newCallbackUrl: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"boxId": "$boxId",
         |"boxName": "$boxName",
         |"oldCallbackUrl": "$oldCallbackUrl",
         |"newCallbackUrl": "$newCallbackUrl"}""".stripMargin

  def validProductionAppNameChangedJsonBody(oldAppName: String, newAppName: String, requestingAdminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "PROD_APP_NAME_CHANGED",
         |"actor": { "user": "$actorUser", "actorType": "$actorTypeGK" },
         |"oldAppName": "$oldAppName",
         |"newAppName": "$newAppName",
         |"requestingAdminEmail": "${requestingAdminEmail.text}"}""".stripMargin

  def validProductionPrivPolicyLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "PROD_APP_PRIVACY_POLICY_LOCATION_CHANGED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"newLocation": {"privacyPolicyType":"url", "value":"$newUrl"},
         |"oldLocation": {"privacyPolicyType":"url", "value":"$oldUrl"},
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validLegacyProductionPrivPolicyLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "PROD_LEGACY_APP_PRIVACY_POLICY_LOCATION_CHANGED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"newUrl": "$newUrl",
         |"oldUrl": "$oldUrl",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validProductionTermsConditionsLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "PROD_APP_TERMS_CONDITIONS_LOCATION_CHANGED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"newLocation": {"termsAndConditionsType":"url", "value":"$newUrl"},
         |"oldLocation": {"termsAndConditionsType":"url", "value":"$oldUrl"},
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validLegacyProductionTermsConditionsLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "PROD_LEGACY_APP_TERMS_CONDITIONS_LOCATION_CHANGED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"newUrl": "$newUrl",
         |"oldUrl": "$oldUrl",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validResponsibleIndividualChangedJsonBody(riName: String, riEmail: LaxEmailAddress, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_CHANGED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"previousResponsibleIndividualName": "Old RI Name",
         |"previousResponsibleIndividualEmail": "old-ri@example.com",
         |"newResponsibleIndividualName": "$riName",
         |"newResponsibleIndividualEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "123456789",
         |"requestingAdminName": "Mr Admin",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validResponsibleIndividualChangedToSelfJsonBody(adminName: String, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_CHANGED_TO_SELF",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"previousResponsibleIndividualName": "Old RI Name",
         |"previousResponsibleIndividualEmail": "old-ri@example.com",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validResponsibleIndividualSetJsonBody(riName: String, riEmail: LaxEmailAddress, adminEmail: LaxEmailAddress, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_SET",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "Mr Admin",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validApplicationStateChangedJsonBody(adminName: String, adminEmail: LaxEmailAddress, oldAppState: String, newAppState: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "APPLICATION_STATE_CHANGED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"oldAppState": "$oldAppState",
         |"newAppState": "$newAppState",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validResponsibleIndividualVerificationStartedJsonBody(riName: String, riEmail: LaxEmailAddress, appName: String, adminName: String, adminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"applicationName": "$appName",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_VERIFICATION_STARTED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}",
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"verificationId": "${UUID.randomUUID().toString}"}""".stripMargin

  def validResponsibleIndividualDeclinedJsonBody(riName: String, riEmail: LaxEmailAddress, adminName: String, adminEmail: LaxEmailAddress, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_DECLINED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validResponsibleIndividualDeclinedUpdateJsonBody(riName: String, riEmail: LaxEmailAddress, adminName: String, adminEmail: LaxEmailAddress, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_DECLINED_UPDATE",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validResponsibleIndividualDidNotVerifyJsonBody(riName: String, riEmail: LaxEmailAddress, adminName: String, adminEmail: LaxEmailAddress, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_DID_NOT_VERIFY",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validApplicationApprovalRequestDeclinedJsonBody(riName: String, riEmail: LaxEmailAddress, adminName: String, adminEmail: LaxEmailAddress, reasons: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "APPLICATION_APPROVAL_REQUEST_DECLINED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"decliningUserName": "$riName",
         |"decliningUserEmail": "${riEmail.text}",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"reasons": "$reasons",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "${adminEmail.text}"}""".stripMargin

  def validApplicationDeletedJsonBody(adminEmail: LaxEmailAddress, wso2ApplicationName: String, reasons: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "APPLICATION_DELETED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"clientId": "$clientIdText",
         |"wso2ApplicationName": "$wso2ApplicationName",
         |"reasons": "$reasons"}""".stripMargin

  def validApplicationDeletedByGatekeeperJsonBody(gatekeeperUser: String, wso2ApplicationName: String, reasons: String, requestingAdminEmail: LaxEmailAddress): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "APPLICATION_DELETED_BY_GATEKEEPER",
         |"actor": { "user": "$gatekeeperUser" },
         |"clientId": "$clientIdText",
         |"wso2ApplicationName": "$wso2ApplicationName",
         |"reasons": "$reasons",
         |"requestingAdminEmail": "${requestingAdminEmail.text}"}""".stripMargin

  def validProductionCredentialsApplicationDeletedJsonBody(adminEmail: LaxEmailAddress, wso2ApplicationName: String, reasons: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$appIdText",
         |"eventDateTime": "$inputInstantString",
         |"eventType": "PRODUCTION_CREDENTIALS_APPLICATION_DELETED",
         |"actor": { "email": "${adminEmail.text}", "actorType": "$actorTypeCollab" },
         |"clientId": "$clientIdText",
         |"wso2ApplicationName": "$wso2ApplicationName",
         |"reasons": "$reasons"}""".stripMargin

  def doGet(path: String): Future[WSResponse] = {
    wsClient
      .url(s"$url$path")
      .get()
  }

  def doPost(path: String, body: String, headers: (String, String)): Future[WSResponse] = {
    wsClient
      .url(s"$url$path")
      .withHttpHeaders(headers)
      .post(body)
  }

  def checkCommonEventValues(event: ApplicationEvent): Unit = {
    event.applicationId shouldBe applicationId
    event.eventDateTime.toString shouldBe expectedEventInstantString
  }

  "ApplicationEventsController" when {

    "POST /ppnsCallbackUriUpdated" should {
      "respond with 201 when valid json is sent" in {
        val boxId          = ju.UUID.randomUUID().toString
        val boxName        = "some##box##name"
        val oldCallbackUrl = "https://foo.bar/baz"
        val newCallbackUrl = "https://foo.bar/bazbazbaz"

        testSuccessScenario("/application-events/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody(boxId, boxName, oldCallbackUrl, newCallbackUrl))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
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

    "POST /application-event" should {
      "respond with 201 when valid prod app name changed json is sent" in {
        val oldAppName           = "old name"
        val newAppName           = "new name"
        val requestingAdminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validProductionAppNameChangedJsonBody(oldAppName, newAppName, requestingAdminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ProductionAppNameChangedEvent]

        checkCommonEventValues(event)
        event.oldAppName.value shouldBe oldAppName
        event.newAppName.value shouldBe newAppName
        event.requestingAdminEmail shouldBe requestingAdminEmail

        event.actor shouldBe Actors.GatekeeperUser(actorUser)

      }

      "respond with 201 when valid prod app privacy policy location changed json is sent" in {
        val oldUrl     = "http://example.com/old"
        val newUrl     = "http://example.com/new"
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validProductionPrivPolicyLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ProductionAppPrivacyPolicyLocationChanged]

        checkCommonEventValues(event)
        event.oldLocation shouldBe PrivacyPolicyLocations.Url(oldUrl)
        event.newLocation shouldBe PrivacyPolicyLocations.Url(newUrl)
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid legacy prod app privacy policy location changed json is sent" in {
        val oldUrl     = "http://example.com/old"
        val newUrl     = "http://example.com/new"
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validLegacyProductionPrivPolicyLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ProductionLegacyAppPrivacyPolicyLocationChanged]

        checkCommonEventValues(event)
        event.oldUrl shouldBe oldUrl
        event.newUrl shouldBe newUrl
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid prod app t&cs location changed json is sent" in {
        val oldUrl     = "http://example.com/old"
        val newUrl     = "http://example.com/new"
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validProductionTermsConditionsLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ProductionAppTermsConditionsLocationChanged]

        checkCommonEventValues(event)
        event.oldLocation shouldBe TermsAndConditionsLocations.Url(oldUrl)
        event.newLocation shouldBe TermsAndConditionsLocations.Url(newUrl)
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid legacy prod app t&cs location changed json is sent" in {
        val oldUrl     = "http://example.com/old"
        val newUrl     = "http://example.com/new"
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validLegacyProductionTermsConditionsLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ProductionLegacyAppTermsConditionsLocationChanged]

        checkCommonEventValues(event)
        event.oldUrl shouldBe oldUrl
        event.newUrl shouldBe newUrl
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid responsible individual changed json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validResponsibleIndividualChangedJsonBody(riName, riEmail, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualChanged]

        checkCommonEventValues(event)
        event.newResponsibleIndividualName shouldBe riName
        event.newResponsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid responsible individual set json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val adminEmail = LaxEmailAddress("admin@example.com")
        val code       = "434235934537645394"

        testSuccessScenario("/application-event", validResponsibleIndividualSetJsonBody(riName, riEmail, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualSet]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.code shouldBe code
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid application state changed json is sent" in {
        val adminName   = "Mr Admin"
        val adminEmail  = LaxEmailAddress("admin@example.com")
        val oldAppState = "PENDING_RESPONSIBLE_INDIVIDUAL_VERIFICATION"
        val newAppState = "PENDING_GATEKEEPER_APPROVAL"

        testSuccessScenario("/application-event", validApplicationStateChangedJsonBody(adminName, adminEmail, oldAppState, newAppState))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ApplicationStateChanged]

        checkCommonEventValues(event)
        event.oldAppState shouldBe oldAppState
        event.newAppState shouldBe newAppState
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
      }

      "respond with 201 when valid responsible individual verification started json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val appName    = "app name"
        val adminName  = "ms admin"
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validResponsibleIndividualVerificationStartedJsonBody(riName, riEmail, appName, adminName, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualVerificationStarted]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.applicationName.value shouldBe appName
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual changed to self json is sent" in {
        val adminName  = "Mr Admin"
        val adminEmail = LaxEmailAddress("admin@example.com")

        testSuccessScenario("/application-event", validResponsibleIndividualChangedToSelfJsonBody(adminName, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualChangedToSelf]

        checkCommonEventValues(event)
        event.requestingAdminName shouldBe adminName
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual declined json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val adminName  = "Mr Admin"
        val adminEmail = LaxEmailAddress("admin@example.com")
        val code       = "324523487236548723458"
        testSuccessScenario("/application-event", validResponsibleIndividualDeclinedJsonBody(riName, riEmail, adminName, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualDeclined]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.code shouldBe code
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual declined update json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val adminName  = "Mr Admin"
        val adminEmail = LaxEmailAddress("admin@example.com")
        val code       = "324523487236548723458"
        testSuccessScenario("/application-event", validResponsibleIndividualDeclinedUpdateJsonBody(riName, riEmail, adminName, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualDeclinedUpdate]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.code shouldBe code
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual did not verify json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val adminName  = "Mr Admin"
        val adminEmail = LaxEmailAddress("admin@example.com")
        val code       = "324523487236548723458"
        testSuccessScenario("/application-event", validResponsibleIndividualDidNotVerifyJsonBody(riName, riEmail, adminName, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ResponsibleIndividualDidNotVerify]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.code shouldBe code
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid application approval request declined json is sent" in {
        val riName     = "Mr Responsible"
        val riEmail    = LaxEmailAddress("ri@example.com")
        val adminName  = "Mr Admin"
        val adminEmail = LaxEmailAddress("admin@example.com")
        val reasons    = "reasons text" +
          ""
        testSuccessScenario("/application-event", validApplicationApprovalRequestDeclinedJsonBody(riName, riEmail, adminName, adminEmail, reasons))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ApplicationApprovalRequestDeclined]

        checkCommonEventValues(event)
        event.decliningUserName shouldBe riName
        event.decliningUserEmail shouldBe riEmail
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.reasons shouldBe reasons
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid application deleted json is sent" in {
        val wso2AppName = "wso2AppName"
        val adminEmail  = LaxEmailAddress("admin@example.com")
        val reasons     = "reasons text"
        testSuccessScenario("/application-event", validApplicationDeletedJsonBody(adminEmail, wso2AppName, reasons))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ApplicationDeleted]

        checkCommonEventValues(event)
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.reasons shouldBe reasons
        event.wso2ApplicationName shouldBe wso2AppName
        event.clientId shouldBe clientId
      }

      "respond with 201 when valid application deleted by gatekeeper json is sent" in {
        val wso2AppName      = "wso2AppName"
        val gatekeeperUser   = "bob smith"
        val reasons          = "reasons text"
        val requestedByEmail = LaxEmailAddress("requester@example.com")
        testSuccessScenario("/application-event", validApplicationDeletedByGatekeeperJsonBody(gatekeeperUser, wso2AppName, reasons, requestedByEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ApplicationDeletedByGatekeeper]

        checkCommonEventValues(event)
        event.actor shouldBe Actors.GatekeeperUser(gatekeeperUser)
        event.reasons shouldBe reasons
        event.wso2ApplicationName shouldBe wso2AppName
        event.clientId shouldBe clientId
        event.requestingAdminEmail shouldBe requestedByEmail
      }

      "respond with 201 when valid production credentials application deleted json is sent" in {
        val wso2AppName = "wso2AppName"
        val adminEmail  = LaxEmailAddress("admin@example.com")
        val reasons     = "reasons text"
        testSuccessScenario("/application-event", validProductionCredentialsApplicationDeletedJsonBody(adminEmail, wso2AppName, reasons))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event   = results.head.asInstanceOf[ProductionCredentialsApplicationDeleted]

        checkCommonEventValues(event)
        event.actor shouldBe Actors.AppCollaborator(adminEmail)
        event.reasons shouldBe reasons
        event.wso2ApplicationName shouldBe wso2AppName
        event.clientId shouldBe clientId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-event")
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
