import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.DefaultBuildSettings
import bloop.integrations.sbt.BloopDefaults
import uk.gov.hmrc.DefaultBuildSettings._

lazy val appName = "api-platform-events"

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(ScoverageSettings())
  .settings(defaultSettings(): _*)
  .settings(
    name := appName,
    PlayKeys.playDefaultPort := 6700,
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= AppDependencies(),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
  )
  .settings(
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    Test / unmanagedSourceDirectories += baseDirectory.value / "testcommon",
    Test / unmanagedSourceDirectories += baseDirectory.value / "test"
  )
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.apiplatformevents.controllers.binders._"
    )
  )
  .settings(
    scalacOptions ++= Seq(
    "-Wconf:cat=unused&src=views/.*\\.scala:s",
    "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
    "-Wconf:cat=unused&src=.*Routes\\.scala:s",
    "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
    )
  )

  lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    Test / unmanagedSourceDirectories += baseDirectory.value / "testcommon"
  )
 
commands ++= Seq(
  Command.command("cleanAll") { state => "clean" :: "it/clean" :: state },
  Command.command("fmtAll") { state => "scalafmtAll" :: "it/scalafmtAll" :: state },
  Command.command("fixAll") { state => "scalafixAll" :: "it/scalafixAll" :: state },
  Command.command("testAll") { state => "test" :: "it/test" :: state },

  Command.command("run-all-tests") { state => "testAll" :: state },

  Command.command("clean-and-test") { state => "cleanAll" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "cleanAll" :: "fmtAll" :: "fixAll" :: "coverage" :: "testAll" :: "coverageOff" :: "coverageAggregate" :: state }
)
