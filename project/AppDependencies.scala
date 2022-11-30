import play.core.PlayVersion
import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  lazy val bootstrapVersion = "7.3.0"
  lazy val hmrcMongoVersion = "0.73.0"

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"        % bootstrapVersion,
    "com.beachape"            %% "enumeratum-play-json"             % "1.7.0",
    "uk.gov.hmrc"             %% "play-json-union-formatter"        % "1.17.0-play-28",
    "com.kenshoo"             %% "metrics-play"                     % "2.7.3_0.8.2",
    "uk.gov.hmrc"             %% "domain"                           % "8.1.0-play-28",
    "com.github.blemale"      %% "scaffeine"                        % "3.1.0",
    "com.enragedginger"       %%  "akka-quartz-scheduler"           % "1.9.1-akka-2.6.x",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"               % hmrcMongoVersion,
    "org.typelevel"           %% "cats-core"                        % "2.1.0",
    "uk.gov.hmrc"             %% "api-platform-application-events"  % "0.1.6"
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"       % bootstrapVersion,
    "org.mockito"             %% "mockito-scala-scalatest"      % "1.16.42",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"      % hmrcMongoVersion,
    "com.github.tomakehurst"  %  "wiremock-jre8-standalone"     % "2.27.1"
  ).map (m => m % "test,it")
}
