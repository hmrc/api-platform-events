import play.core.PlayVersion
import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  private lazy val dependencies = Seq(
    "uk.gov.hmrc"         %% "bootstrap-play-26"          % "4.0.0",
    "com.beachape"        %% "enumeratum-play-json"       % "1.6.0",
    "uk.gov.hmrc"         %% "play-json-union-formatter"  % "1.12.0-play-26",
    "com.kenshoo"         %% "metrics-play"               % "2.6.19_0.7.0",
    "uk.gov.hmrc"         %% "domain"                     % "5.11.0-play-26",
    "com.github.blemale"  %% "scaffeine"                  % "3.1.0",
    "uk.gov.hmrc"         %% "agent-kenshoo-monitoring"   % "4.4.0",
    "uk.gov.hmrc"         %% "simple-reactivemongo"       % "7.30.0-play-26",
    "uk.gov.hmrc"         %% "play-scheduling"            % "7.4.0-play-26",
    "org.reactivemongo"   %% "reactivemongo-akkastream"   % "0.20.11",
    "com.typesafe.play"   %% "play-json-joda"             % "2.7.4"
  )

  private lazy val testDependencies = Seq(
    "org.mockito"             %% "mockito-scala-scalatest"      % "1.7.1",
    "org.scalatestplus.play"  %% "scalatestplus-play"           % "3.1.3",
    "uk.gov.hmrc"             %% "reactivemongo-test"           % "4.22.0-play-26",
    "com.github.tomakehurst"  %  "wiremock-jre8-standalone"     % "2.27.1"
  ).map (m => m % "test,it")
}
