import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  lazy val bootstrapVersion    = "7.15.0"
  lazy val hmrcMongoVersion    = "1.7.0"
  lazy val commonDomainVersion = "0.10.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28"       % bootstrapVersion,
    "com.kenshoo"        %% "metrics-play"                    % "2.7.3_0.8.2",
    "uk.gov.hmrc"        %% "domain"                          % "8.1.0-play-28",
    "com.github.blemale" %% "scaffeine"                       % "5.2.1",
    "com.enragedginger"  %% "akka-quartz-scheduler"           % "1.9.1-akka-2.6.x",
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-28"              % hmrcMongoVersion,
    "uk.gov.hmrc"        %% "api-platform-application-events" % "0.38.0"
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"          % bootstrapVersion,
    "org.mockito"           %% "mockito-scala-scalatest"         % "1.17.29",
    "org.scalatest"         %% "scalatest"                       % "3.2.17",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-28"         % hmrcMongoVersion,
    "com.github.tomakehurst" % "wiremock-jre8-standalone"        % "2.35.0",
    "uk.gov.hmrc"           %% "api-platform-test-common-domain" % commonDomainVersion
  ).map(m => m % "test,it")
}
