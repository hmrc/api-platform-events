import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  lazy val bootstrapVersion    = "9.19.0"
  lazy val hmrcMongoVersion    = "2.7.0"
  lazy val appEventsVersion    = "0.88.0" // Ensure this version of the application-events library uses the appDomainVersion below
  lazy val appDomainVersion    = "0.87.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"            %% "bootstrap-backend-play-30"       % bootstrapVersion,
    "io.github.samueleresca" %% "pekko-quartz-scheduler"          % "1.1.0-pekko-1.0.x",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-30"              % hmrcMongoVersion,
    "uk.gov.hmrc"            %% "api-platform-application-events" % appEventsVersion

  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"              % bootstrapVersion,
    "org.mockito"            %% "mockito-scala-scalatest"             % "1.17.29",
    "org.scalatest"          %% "scalatest"                           % "3.2.17",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"             % hmrcMongoVersion,
    "com.github.tomakehurst"  % "wiremock-jre8-standalone"            % "2.35.0",
    "uk.gov.hmrc"            %% "api-platform-application-domain-fixtures" % appDomainVersion
  ).map(m => m % "test")
}
