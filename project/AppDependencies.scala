import sbt.*

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  lazy val bootstrapVersion    = "10.8.0"
  lazy val hmrcMongoVersion    = "2.13.0"

  lazy val commonDomainVersion = "1.4.0"
  lazy val appEventsVersion    = "1.3.0"
  lazy val appDomainVersion    = "1.6.0"

  private val dependencies = Seq(
    "uk.gov.hmrc"            %% "bootstrap-backend-play-30"       % bootstrapVersion,
    "io.github.samueleresca" %% "pekko-quartz-scheduler"          % "1.2.2-pekko-1.0.x",  // 1.0.x from play framework
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-30"              % hmrcMongoVersion,
    "uk.gov.hmrc"            %% "api-platform-common-domain"      % commonDomainVersion,
    "uk.gov.hmrc"            %% "api-platform-application-domain" % appDomainVersion,
    "uk.gov.hmrc"            %% "api-platform-application-events" % appEventsVersion
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"              % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"             % hmrcMongoVersion,
    "uk.gov.hmrc"            %% "api-platform-common-domain-fixtures" % commonDomainVersion,
    "uk.gov.hmrc"            %% "api-platform-application-domain-fixtures" % appDomainVersion
  ).map(m => m % "test")
}
 