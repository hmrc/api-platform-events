import play.core.PlayVersion
import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"    % "5.14.0",
    "com.beachape"            %% "enumeratum-play-json"         % "1.7.0",
    "uk.gov.hmrc"             %% "play-json-union-formatter"    % "1.15.0-play-28",
    "com.kenshoo"             %% "metrics-play"                 % "2.7.3_0.8.2",
    "uk.gov.hmrc"             %% "domain"                       % "6.2.0-play-28",
    "com.github.blemale"      %% "scaffeine"                    % "3.1.0",
    "uk.gov.hmrc"             %% "agent-kenshoo-monitoring"     % "4.8.0-play-28",
    "uk.gov.hmrc"             %% "simple-reactivemongo"         % "8.0.0-play-28",
    "org.reactivemongo"       %% "reactivemongo-akkastream"     % "0.18.8",
    "com.typesafe.play"       %% "play-json-joda"               % "2.8.1",
    "uk.gov.hmrc"             %% "mongo-lock"                   % "7.0.0-play-28",
  )

  private lazy val testDependencies = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"       % "5.14.0",
    "org.mockito"             %% "mockito-scala-scalatest"      % "1.16.42",
    "uk.gov.hmrc"             %% "reactivemongo-test"           % "5.0.0-play-28",
    "com.github.tomakehurst"  %  "wiremock-jre8-standalone"     % "2.27.1"
  ).map (m => m % "test,it")
}
