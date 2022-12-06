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

package uk.gov.hmrc.apiplatformevents.data

import java.time.LocalDateTime
import uk.gov.hmrc.apiplatform.modules.applications.domain.models._
import uk.gov.hmrc.apiplatform.modules.events.applications.domain.models._
import java.util.UUID
import uk.gov.hmrc.apiplatform.modules.apis.domain.models.ApiIdentifier
import uk.gov.hmrc.apiplatform.modules.apis.domain.models.ApiContext
import uk.gov.hmrc.apiplatform.modules.apis.domain.models.ApiVersion

trait ApplicationEventTestData {
  val teamMemberAddedModel: TeamMemberAddedEvent = TeamMemberAddedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    teamMemberEmail = LaxEmailAddress("jkhkhk"),
    teamMemberRole = "ADMIN")

  val collaboratorAdded: CollaboratorAdded = CollaboratorAdded(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    Collaborators.Administrator("someId", LaxEmailAddress("jkhkhk")),
    verifiedAdminsToEmail = Set(LaxEmailAddress("email")))

  def makeTeamMemberAddedEvent(appId: Option[ApplicationId] = None): TeamMemberAddedEvent = {
    teamMemberAddedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  def makeCollaboratorAdded(appId: Option[ApplicationId] = None): CollaboratorAdded = {
    collaboratorAdded.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val teamMemberRemovedModel: TeamMemberRemovedEvent = TeamMemberRemovedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    teamMemberEmail = LaxEmailAddress("jkhkhk"),
    teamMemberRole = "ADMIN")

  val collaboratorRemoved: CollaboratorRemoved = CollaboratorRemoved(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    Collaborators.Administrator("someId", LaxEmailAddress("jkhkhk")),
    verifiedAdminsToEmail= Set("email1", "email2", "email3").map(LaxEmailAddress(_)))

  def makeTeamMemberRemovedEvent(appId: Option[ApplicationId] = None): TeamMemberRemovedEvent = {
    teamMemberRemovedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  def makeCollaboratorRemoved(appId: Option[ApplicationId] = None): CollaboratorRemoved = {
    collaboratorRemoved.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }


  val clientSecretAddedModel: ClientSecretAddedEvent = ClientSecretAddedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    clientSecretId = "jkhkhk")

  val clientSecretAddedV2Model: ClientSecretAdded = ClientSecretAdded(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    clientSecretId = "jkhkhk",
    clientSecretName = "****hkhk")

  def makeClientSecretAddedEvent(appId: Option[ApplicationId] = None): ClientSecretAddedEvent = {
    clientSecretAddedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  def makeClientSecretAdded(appId: Option[ApplicationId] = None): ClientSecretAdded = {
    clientSecretAddedV2Model.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val clientSecretRemovedModel: ClientSecretRemovedEvent = ClientSecretRemovedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    clientSecretId = "jkhkhk")

  val clientSecretRemovedV2Model: ClientSecretRemoved = ClientSecretRemoved(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    clientSecretId = "jkhkhk",
    clientSecretName = "****hkhk")

  def makeClientSecretRemovedEvent(appId: Option[ApplicationId] = None): ClientSecretRemovedEvent = {
    clientSecretRemovedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  def makeClientSecretRemoved(appId: Option[ApplicationId] = None): ClientSecretRemoved = {
    clientSecretRemovedV2Model.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val redirectUrisUpdatedModel: RedirectUrisUpdatedEvent = RedirectUrisUpdatedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    oldRedirectUris = "oldru",
    newRedirectUris = "newru")

  def makeRedirectUrisUpdatedEvent(appId: Option[ApplicationId] = None): RedirectUrisUpdatedEvent = {
    redirectUrisUpdatedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val redirectUrisUpdatedV2Model: RedirectUrisUpdated = RedirectUrisUpdated(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldRedirectUris = List("oldru"),
    newRedirectUris = List("newru", "newuri2"))

  def makeRedirectUrisUpdated(appId: Option[ApplicationId] = None): RedirectUrisUpdated = {
    redirectUrisUpdatedV2Model.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val apiSubscribedModel: ApiSubscribedEvent = ApiSubscribedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    context = "apicontext",
    version = "1.0")

  val apiSubscribedV2Model: ApiSubscribed = ApiSubscribed(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    ApiIdentifier(ApiContext("apicontext"), ApiVersion("1.0"))
  )

  def makeApiSubscribedEvent(appId: Option[ApplicationId] = None): ApiSubscribedEvent = {
    apiSubscribedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  def makeApiSubscribed(appId: Option[ApplicationId] = None): ApiSubscribed = {
    apiSubscribedV2Model.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val apiUnsubscribedModel: ApiUnsubscribedEvent = ApiUnsubscribedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    context = "apicontext",
    version = "1.0")

  val apiUnsubscribedV2Model: ApiUnsubscribed = ApiUnsubscribed(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    ApiIdentifier(ApiContext("apicontext"), ApiVersion("1.0"))
    )

  def makeApiUnsubscribedEvent(appId: Option[ApplicationId] = None): ApiUnsubscribedEvent = {
    apiUnsubscribedModel.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  def makeApiUnsubscribed(appId: Option[ApplicationId] = None): ApiUnsubscribed = {
    apiUnsubscribedV2Model.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val ppnsCallBackUriUpdatedEvent: PpnsCallBackUriUpdatedEvent = PpnsCallBackUriUpdatedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    OldStyleActors.GatekeeperUser("iam@admin.com"),
    boxId = "boxId",
    boxName = "boxName",
    oldCallbackUrl = "some/url/",
    newCallbackUrl = "some/url/here")

  def makePpnsCallBackUriUpdatedEvent(appId: Option[ApplicationId] = None): PpnsCallBackUriUpdatedEvent = {
    ppnsCallBackUriUpdatedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val productionAppNameChangedEvent: ProductionAppNameChangedEvent = ProductionAppNameChangedEvent(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldAppName = "old app name",
    newAppName = "new app name",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeProductionAppNameChangedEvent(appId: Option[ApplicationId] = None): ProductionAppNameChangedEvent = {
    productionAppNameChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val productionAppPrivacyPolicyLocationChangedEvent: ProductionAppPrivacyPolicyLocationChanged = ProductionAppPrivacyPolicyLocationChanged(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldLocation = PrivacyPolicyLocations.InDesktopSoftware,
    newLocation = PrivacyPolicyLocations.Url("http://example.com"),
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeProductionAppPrivacyPolicyLocationChangedEvent(appId: Option[ApplicationId] = None): ProductionAppPrivacyPolicyLocationChanged = {
    productionAppPrivacyPolicyLocationChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val productionLegacyAppPrivacyPolicyLocationChangedEvent: ProductionLegacyAppPrivacyPolicyLocationChanged = ProductionLegacyAppPrivacyPolicyLocationChanged(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldUrl = "http://example.com/old",
    newUrl = "http://example.com/new",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeProductionLegacyAppPrivacyPolicyLocationChanged(appId: Option[ApplicationId] = None): ProductionLegacyAppPrivacyPolicyLocationChanged = {
    productionLegacyAppPrivacyPolicyLocationChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val productionAppTermsConditionsLocationChangedEvent: ProductionAppTermsConditionsLocationChanged = ProductionAppTermsConditionsLocationChanged(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldLocation = TermsAndConditionsLocations.InDesktopSoftware,
    newLocation = TermsAndConditionsLocations.Url("http://example.com"),
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeProductionAppTermsConditionsLocationChanged(appId: Option[ApplicationId] = None): ProductionAppTermsConditionsLocationChanged = {
    productionAppTermsConditionsLocationChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val productionLegacyAppTermsConditionsLocationChangedEvent: ProductionLegacyAppTermsConditionsLocationChanged = ProductionLegacyAppTermsConditionsLocationChanged(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldUrl = "http://example.com/old",
    newUrl = "http://example.com/new",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeProductionLegacyAppTermsConditionsLocationChanged(appId: Option[ApplicationId] = None): ProductionLegacyAppTermsConditionsLocationChanged = {
    productionLegacyAppTermsConditionsLocationChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualChangedEvent: ResponsibleIndividualChanged = ResponsibleIndividualChanged(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    previousResponsibleIndividualName = "Mr Old Responsible",
    previousResponsibleIndividualEmail = LaxEmailAddress("old-ri@example.com"),
    newResponsibleIndividualName = "Mr Responsible",
    newResponsibleIndividualEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    code = "123456789",
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeResponsibleIndividualChanged(appId: Option[ApplicationId] = None): ResponsibleIndividualChanged = {
    responsibleIndividualChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualChangedToSelfEvent: ResponsibleIndividualChangedToSelf = ResponsibleIndividualChangedToSelf(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    previousResponsibleIndividualName = "Mr Old Responsible",
    previousResponsibleIndividualEmail = LaxEmailAddress("old-ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeResponsibleIndividualChangedToSelf(appId: Option[ApplicationId] = None): ResponsibleIndividualChangedToSelf = {
    responsibleIndividualChangedToSelfEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualSetEvent: ResponsibleIndividualSet = ResponsibleIndividualSet(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    responsibleIndividualName = "Mr Responsible",
    responsibleIndividualEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    code = UUID.randomUUID().toString,
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeResponsibleIndividualSet(appId: Option[ApplicationId] = None): ResponsibleIndividualSet = {
    responsibleIndividualSetEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val applicationStateChangedEvent: ApplicationStateChanged = ApplicationStateChanged(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    oldAppState = "PENDING_RESPONSIBLE_INDIVIDUAL_VERIFICATION",
    newAppState = "PENDING_GATEKEEPER_APPROVAL",
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeApplicationStateChanged(appId: Option[ApplicationId] = None): ApplicationStateChanged = {
    applicationStateChangedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualVerificationStarted: ResponsibleIndividualVerificationStarted = ResponsibleIndividualVerificationStarted(
    id = EventId.random,
    applicationId = ApplicationId.random,
    applicationName = "my app",
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"),
    responsibleIndividualName = "Mr Responsible",
    responsibleIndividualEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    verificationId = UUID.randomUUID().toString)

  def makeResponsibleIndividualVerificationStarted(appId: Option[ApplicationId] = None): ResponsibleIndividualVerificationStarted = {
    responsibleIndividualVerificationStarted.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualDeclinedEvent: ResponsibleIndividualDeclined = ResponsibleIndividualDeclined(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    responsibleIndividualName = "Mr Responsible",
    responsibleIndividualEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    code = "123456789",
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeResponsibleIndividualDeclined(appId: Option[ApplicationId] = None): ResponsibleIndividualDeclined = {
    responsibleIndividualDeclinedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualDeclinedUpdateEvent: ResponsibleIndividualDeclinedUpdate = ResponsibleIndividualDeclinedUpdate(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    responsibleIndividualName = "Mr Responsible",
    responsibleIndividualEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    code = "123456789",
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeResponsibleIndividualDeclinedUpdate(appId: Option[ApplicationId] = None): ResponsibleIndividualDeclinedUpdate = {
    responsibleIndividualDeclinedUpdateEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val responsibleIndividualDidNotVerifyEvent: ResponsibleIndividualDidNotVerify = ResponsibleIndividualDidNotVerify(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    responsibleIndividualName = "Mr Responsible",
    responsibleIndividualEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    code = "123456789",
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeResponsibleIndividualDidNotVerify(appId: Option[ApplicationId] = None): ResponsibleIndividualDidNotVerify = {
    responsibleIndividualDidNotVerifyEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }

  val applicationApprovalRequestDeclinedEvent: ApplicationApprovalRequestDeclined = ApplicationApprovalRequestDeclined(
    id = EventId.random,
    applicationId = ApplicationId.random,
    eventDateTime = LocalDateTime.now(),
    actor = Actors.Collaborator(LaxEmailAddress("iam@admin.com")),
    decliningUserName = "Mr Responsible",
    decliningUserEmail = LaxEmailAddress("ri@example.com"),
    submissionId = UUID.randomUUID().toString,
    submissionIndex = 1,
    reasons = "reason text",
    requestingAdminName = "Mr Admin",
    requestingAdminEmail = LaxEmailAddress("admin@example.com"))

  def makeApplicationApprovalRequestDeclined(appId: Option[ApplicationId] = None): ApplicationApprovalRequestDeclined = {
    applicationApprovalRequestDeclinedEvent.copy(applicationId = appId.fold(ApplicationId.random)(identity), id = EventId.random, eventDateTime = LocalDateTime.now())
  }    
}

