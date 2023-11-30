import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import bloop.integrations.sbt.BloopDefaults

lazy val appName = "api-platform-events"

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .settings(
    name := appName,
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.12",
    PlayKeys.playDefaultPort := 6700,
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= AppDependencies(),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)

  .settings(ScoverageSettings())

  .settings(
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    Test / parallelExecution := false,
    Test / unmanagedSourceDirectories += baseDirectory.value / "testcommon",
    Test / unmanagedSourceDirectories += baseDirectory.value / "test"
  )

  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    IntegrationTest / Keys.fork := false,
    IntegrationTest / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "testcommon",
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "it",
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value)
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

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}

commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: "it:test" :: state },

  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "clean" :: "scalafmtAll" :: "scalafixAll" ::"coverage" :: "run-all-tests" :: "coverageReport" :: "coverageOff" :: state }
)