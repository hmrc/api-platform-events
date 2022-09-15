package uk.gov.hmrc.apiplatformevents.controllers

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.apiplatformevents.models._
import uk.gov.hmrc.apiplatformevents.models.common.{ActorType, CollaboratorActor, EventId, GatekeeperUserActor}
import uk.gov.hmrc.apiplatformevents.repository.ApplicationEventsRepository
import uk.gov.hmrc.apiplatformevents.support.{AuditService, ServerBaseISpec}

import java.util.UUID
import java.{util => ju}
import scala.concurrent.Future

class ApplicationEventsControllerISpec extends ServerBaseISpec  with AuditService with BeforeAndAfterEach {

  this: Suite with ServerProvider =>

  def repo: ApplicationEventsRepository = app.injector.instanceOf[ApplicationEventsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    primeAuditService()
    await(repo.collection.drop().toFuture())
  }

  val url = s"http://localhost:$port"

  val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val eventId: UUID = EventId.random.value
  val applicationId = UUID.randomUUID().toString
  val submissionId: String = ju.UUID.randomUUID.toString
  val actorId = "123454654"
  val actorEmail = "actor@example.com"
  val actorTypeGK = ActorType.GATEKEEPER
  val actorTypeCollab = ActorType.COLLABORATOR
  val actorUser = "gatekeeper"
  val eventDateTimeString = "2014-01-01T13:13:34.441"

  def validTeamMemberJsonBody(teamMemberEmail: String, teamMemberRole: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"teamMemberEmail": "$teamMemberEmail",
         |"teamMemberRole": "$teamMemberRole"}""".stripMargin

  def validClientSecretJsonBody(clientSecretId: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"clientSecretId": "$clientSecretId"}""".stripMargin

  def validRedirectUrisUpdatedJsonBody(oldRedirectUri: String, newRedirectUri: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"oldRedirectUris": "$oldRedirectUri",
         |"newRedirectUris": "$newRedirectUri"}""".stripMargin

  def validApiSubscriptionJsonBody(apiContext: String, apiVersion: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"context": "$apiContext",
         |"version": "$apiVersion"}""".stripMargin

  def validPpnsCallBackUpdatedJsonBody(boxId: String, boxName: String, oldCallbackUrl: String, newCallbackUrl: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"actor": { "id": "$actorId", "actorType": "$actorTypeGK" },
         |"boxId": "$boxId",
         |"boxName": "$boxName",
         |"oldCallbackUrl": "$oldCallbackUrl",
         |"newCallbackUrl": "$newCallbackUrl"}""".stripMargin

  def validProductionAppNameChangedJsonBody(oldAppName: String, newAppName: String, requestingAdminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "PROD_APP_NAME_CHANGED",
         |"actor": { "user": "$actorUser", "actorType": "$actorTypeGK" },
         |"oldAppName": "$oldAppName",
         |"newAppName": "$newAppName",
         |"requestingAdminEmail": "$requestingAdminEmail"}""".stripMargin

  def validProductionPrivPolicyLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "PROD_APP_PRIVACY_POLICY_LOCATION_CHANGED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"newLocation": {"privacyPolicyType":"url", "value":"$newUrl"},
         |"oldLocation": {"privacyPolicyType":"url", "value":"$oldUrl"},
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validLegacyProductionPrivPolicyLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "PROD_LEGACY_APP_PRIVACY_POLICY_LOCATION_CHANGED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"newUrl": "$newUrl",
         |"oldUrl": "$oldUrl",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validProductionTermsConditionsLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "PROD_APP_TERMS_CONDITIONS_LOCATION_CHANGED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"newLocation": {"termsAndConditionsType":"url", "value":"$newUrl"},
         |"oldLocation": {"termsAndConditionsType":"url", "value":"$oldUrl"},
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validLegacyProductionTermsConditionsLocationChangedJsonBody(oldUrl: String, newUrl: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "PROD_LEGACY_APP_TERMS_CONDITIONS_LOCATION_CHANGED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"newUrl": "$newUrl",
         |"oldUrl": "$oldUrl",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validResponsibleIndividualChangedJsonBody(riName: String, riEmail: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_CHANGED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"previousResponsibleIndividualName": "Old RI Name",
         |"previousResponsibleIndividualEmail": "old-ri@example.com",
         |"newResponsibleIndividualName": "$riName",
         |"newResponsibleIndividualEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "123456789",
         |"requestingAdminName": "Mr Admin",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validResponsibleIndividualChangedToSelfJsonBody(adminName: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_CHANGED_TO_SELF",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"previousResponsibleIndividualName": "Old RI Name",
         |"previousResponsibleIndividualEmail": "old-ri@example.com",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validResponsibleIndividualSetJsonBody(riName: String, riEmail: String, adminEmail: String, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_SET",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "Mr Admin",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validApplicationStateChangedJsonBody(adminName: String, adminEmail: String, oldAppState: String, newAppState: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "APPLICATION_STATE_CHANGED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"oldAppState": "$oldAppState",
         |"newAppState": "$newAppState",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validResponsibleIndividualVerificationStartedJsonBody(riName: String, riEmail: String, appName: String, adminName: String, adminEmail: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"applicationName": "$appName",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_VERIFICATION_STARTED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail",
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"verificationId": "${UUID.randomUUID().toString}"}""".stripMargin

  def validResponsibleIndividualDeclinedJsonBody(riName: String, riEmail: String, adminName: String, adminEmail: String, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_DECLINED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validResponsibleIndividualDeclinedUpdateJsonBody(riName: String, riEmail: String, adminName: String, adminEmail: String, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_DECLINED_UPDATE",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validResponsibleIndividualDidNotVerifyJsonBody(riName: String, riEmail: String, adminName: String, adminEmail: String, code: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "RESPONSIBLE_INDIVIDUAL_DID_NOT_VERIFY",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"responsibleIndividualName": "$riName",
         |"responsibleIndividualEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"code": "$code",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

  def validApplicationApprovalRequestDeclinedJsonBody(riName: String, riEmail: String, adminName: String, adminEmail: String, reasons: String): String =
    raw"""{"id": "${EventId.random.value}",
         |"applicationId": "$applicationId",
         |"eventDateTime": "$eventDateTimeString",
         |"eventType": "APPLICATION_APPROVAL_REQUEST_DECLINED",
         |"actor": { "email": "$adminEmail", "actorType": "$actorTypeCollab" },
         |"decliningUserName": "$riName",
         |"decliningUserEmail": "$riEmail",
         |"submissionId": "$submissionId",
         |"submissionIndex": 1,
         |"reasons": "$reasons",
         |"requestingAdminName": "$adminName",
         |"requestingAdminEmail": "$adminEmail"}""".stripMargin

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
    event.eventDateTime.toString shouldBe eventDateTimeString
  }

  "ApplicationEventsController" when {

    "POST /teamMemberAdded" should {
      "respond with 201 when valid json is sent" in {
        val teamMemberEmail = "bob@bob.com"
        val adminRole = "ADMIN"

        testSuccessScenario("/application-events/teamMemberAdded", validTeamMemberJsonBody(teamMemberEmail, adminRole))
        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[TeamMemberAddedEvent]

        checkCommonEventValues(event)
        event.teamMemberEmail shouldBe teamMemberEmail
        event.teamMemberRole shouldBe adminRole
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/teamMemberAdded")
      }
    }

    "POST /teamMemberRemoved" should {
      "respond with 201 when valid json is sent" in {
        val teamMemberEmail = "bob@bob.com"
        val adminRole = "ADMIN"

        testSuccessScenario("/application-events/teamMemberRemoved", validTeamMemberJsonBody(teamMemberEmail, adminRole))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[TeamMemberRemovedEvent]

        checkCommonEventValues(event)
        event.teamMemberEmail shouldBe teamMemberEmail
        event.teamMemberRole shouldBe adminRole
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/teamMemberRemoved")
      }
    }

    "POST /clientSecretAdded" should {
      "respond with 201 when valid json is sent" in {
        val clientSecretId = ju.UUID.randomUUID().toString

        testSuccessScenario("/application-events/clientSecretAdded", validClientSecretJsonBody(clientSecretId))


        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ClientSecretAddedEvent]

        checkCommonEventValues(event)
        event.clientSecretId shouldBe clientSecretId
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/clientSecretAdded")
      }
    }

    "POST /clientSecretRemoved" should {
      "respond with 201 when valid json is sent" in {
        val clientSecretId = ju.UUID.randomUUID().toString

        testSuccessScenario("/application-events/clientSecretRemoved", validClientSecretJsonBody(clientSecretId))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ClientSecretRemovedEvent]

        checkCommonEventValues(event)
        event.clientSecretId shouldBe clientSecretId
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/clientSecretRemoved")
      }
    }

    "POST /redirectUrisUpdated" should {
      "respond with 201 when valid json is sent" in {
        val oldRedirectUri = "oldrdu"
        val newRedirectUri = "newrdu"

        testSuccessScenario("/application-events/redirectUrisUpdated", validRedirectUrisUpdatedJsonBody(oldRedirectUri, newRedirectUri))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[RedirectUrisUpdatedEvent]

        checkCommonEventValues(event)
        event.oldRedirectUris shouldBe oldRedirectUri
        event.newRedirectUris shouldBe newRedirectUri
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/redirectUrisUpdated")
      }
    }

    "POST /apiSubscribed" should {
      "respond with 201 when valid json is sent" in {
        val apiContext = "apicontext"
        val apiVersion = "1.0"

        testSuccessScenario("/application-events/apiSubscribed", validApiSubscriptionJsonBody(apiContext, apiVersion))
        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApiSubscribedEvent]

        checkCommonEventValues(event)
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/apiSubscribed")
      }
    }

    "POST /apiUnsubscribed" should {
      "respond with 201 when valid json is sent" in {
        val apiContext = "apicontext"
        val apiVersion = "1.0"

        testSuccessScenario("/application-events/apiUnsubscribed", validApiSubscriptionJsonBody(apiContext, apiVersion))
        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApiUnsubscribedEvent]

        checkCommonEventValues(event)
        event.context shouldBe apiContext
        event.version shouldBe apiVersion
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/apiUnsubscribed")
      }
    }

    "POST /ppnsCallbackUriUpdated" should {
      "respond with 201 when valid json is sent" in {
        val boxId = ju.UUID.randomUUID().toString
        val boxName = "some##box##name"
        val oldCallbackUrl = "https://foo.bar/baz"
        val newCallbackUrl = "https://foo.bar/bazbazbaz"

        testSuccessScenario("/application-events/ppnsCallbackUriUpdated", validPpnsCallBackUpdatedJsonBody(boxId, boxName, oldCallbackUrl, newCallbackUrl))

        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[PpnsCallBackUriUpdatedEvent]

        checkCommonEventValues(event)
        event.boxId shouldBe boxId
        event.oldCallbackUrl shouldBe oldCallbackUrl
        event.newCallbackUrl shouldBe newCallbackUrl
        event.boxName shouldBe boxName
        event.actor.actorType shouldBe actorTypeGK
        event.actor.id shouldBe actorId
      }

      "handle error scenarios correctly" in {
        testErrorScenarios("/application-events/ppnsCallbackUriUpdated")
      }

    }

    "POST /application-event" should {
      "respond with 201 when valid prod app name changed json is sent" in {
        val oldAppName = "old name"
        val newAppName = "new name"
        val requestingAdminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validProductionAppNameChangedJsonBody(oldAppName, newAppName, requestingAdminEmail))

        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ProductionAppNameChangedEvent]

        checkCommonEventValues(event)
        event.oldAppName shouldBe oldAppName
        event.newAppName shouldBe newAppName
        event.requestingAdminEmail shouldBe requestingAdminEmail

        event.actor match {
          case GatekeeperUserActor(name) => name shouldBe actorUser
          case _ => fail("expected GatekeeperUserActor")
        }

      }

      "respond with 201 when valid prod app privacy policy location changed json is sent" in {
        val oldUrl = "http://example.com/old"
        val newUrl = "http://example.com/new"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validProductionPrivPolicyLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ProductionAppPrivacyPolicyLocationChanged]

        checkCommonEventValues(event)
        event.oldLocation shouldBe PrivacyPolicyLocation.Url(oldUrl)
        event.newLocation shouldBe PrivacyPolicyLocation.Url(newUrl)
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid legacy prod app privacy policy location changed json is sent" in {
        val oldUrl = "http://example.com/old"
        val newUrl = "http://example.com/new"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validLegacyProductionPrivPolicyLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results =await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ProductionLegacyAppPrivacyPolicyLocationChanged]

        checkCommonEventValues(event)
        event.oldUrl shouldBe oldUrl
        event.newUrl shouldBe newUrl
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid prod app t&cs location changed json is sent" in {
        val oldUrl = "http://example.com/old"
        val newUrl = "http://example.com/new"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validProductionTermsConditionsLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ProductionAppTermsConditionsLocationChanged]

        checkCommonEventValues(event)
        event.oldLocation shouldBe TermsAndConditionsLocation.Url(oldUrl)
        event.newLocation shouldBe TermsAndConditionsLocation.Url(newUrl)
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid legacy prod app t&cs location changed json is sent" in {
        val oldUrl = "http://example.com/old"
        val newUrl = "http://example.com/new"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validLegacyProductionTermsConditionsLocationChangedJsonBody(oldUrl, newUrl, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ProductionLegacyAppTermsConditionsLocationChanged]

        checkCommonEventValues(event)
        event.oldUrl shouldBe oldUrl
        event.newUrl shouldBe newUrl
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid responsible individual changed json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validResponsibleIndividualChangedJsonBody(riName, riEmail, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualChanged]

        checkCommonEventValues(event)
        event.newResponsibleIndividualName shouldBe riName
        event.newResponsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid responsible individual set json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val adminEmail = "admin@example.com"
        val code = "434235934537645394"

        testSuccessScenario("/application-event", validResponsibleIndividualSetJsonBody(riName, riEmail, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualSet]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.code shouldBe code
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid application state changed json is sent" in {
        val adminName = "Mr Admin"
        val adminEmail = "admin@example.com"
        val oldAppState = "PENDING_RESPONSIBLE_INDIVIDUAL_VERIFICATION"
        val newAppState = "PENDING_GATEKEEPER_APPROVAL"

        testSuccessScenario("/application-event", validApplicationStateChangedJsonBody(adminName, adminEmail, oldAppState, newAppState))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApplicationStateChanged]

        checkCommonEventValues(event)
        event.oldAppState shouldBe oldAppState
        event.newAppState shouldBe newAppState
        event.actor shouldBe CollaboratorActor(adminEmail)
      }

      "respond with 201 when valid responsible individual verification started json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val appName = "app name"
        val adminName = "ms admin"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validResponsibleIndividualVerificationStartedJsonBody(riName, riEmail, appName, adminName, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualVerificationStarted]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.applicationName shouldBe appName
        event.actor shouldBe CollaboratorActor(adminEmail)
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual changed to self json is sent" in {
        val adminName = "Mr Admin"
        val adminEmail = "admin@example.com"

        testSuccessScenario("/application-event", validResponsibleIndividualChangedToSelfJsonBody(adminName, adminEmail))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualChangedToSelf]

        checkCommonEventValues(event)
        event.requestingAdminName shouldBe adminName
        event.actor shouldBe CollaboratorActor(adminEmail)
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual declined json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val adminName = "Mr Admin"
        val adminEmail = "admin@example.com"
        val code = "324523487236548723458"
        testSuccessScenario("/application-event", validResponsibleIndividualDeclinedJsonBody(riName, riEmail, adminName, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualDeclined]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe CollaboratorActor(adminEmail)
        event.code shouldBe code
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual declined update json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val adminName = "Mr Admin"
        val adminEmail = "admin@example.com"
        val code = "324523487236548723458"
        testSuccessScenario("/application-event", validResponsibleIndividualDeclinedUpdateJsonBody(riName, riEmail, adminName, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualDeclinedUpdate]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe CollaboratorActor(adminEmail)
        event.code shouldBe code
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid responsible individual did not verify json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val adminName = "Mr Admin"
        val adminEmail = "admin@example.com"
        val code = "324523487236548723458"
        testSuccessScenario("/application-event", validResponsibleIndividualDidNotVerifyJsonBody(riName, riEmail, adminName, adminEmail, code))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ResponsibleIndividualDidNotVerify]

        checkCommonEventValues(event)
        event.responsibleIndividualName shouldBe riName
        event.responsibleIndividualEmail shouldBe riEmail
        event.actor shouldBe CollaboratorActor(adminEmail)
        event.code shouldBe code
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
      }

      "respond with 201 when valid application approval request declined json is sent" in {
        val riName = "Mr Responsible"
        val riEmail = "ri@example.com"
        val adminName = "Mr Admin"
        val adminEmail = "admin@example.com"
        val reasons = "reasons text" +
          ""
        testSuccessScenario("/application-event", validApplicationApprovalRequestDeclinedJsonBody(riName, riEmail, adminName, adminEmail, reasons))

        val results = await(repo.collection.find().toFuture())
        results.size shouldBe 1
        val event = results.head.asInstanceOf[ApplicationApprovalRequestDeclined]

        checkCommonEventValues(event)
        event.decliningUserName shouldBe riName
        event.decliningUserEmail shouldBe riEmail
        event.actor shouldBe CollaboratorActor(adminEmail)
        event.reasons shouldBe reasons
        event.requestingAdminName shouldBe adminName
        event.requestingAdminEmail shouldBe adminEmail
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
      // val result = await(doPost(uriToTest, "i'm not JSON", "Content-Type" -> "application/json"))
      // withClue("should respond with 400 when invalid json is sent") {
      //   result.status shouldBe 400
      //   result.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
      // }

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
