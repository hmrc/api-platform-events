import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import bloop.integrations.sbt.BloopDefaults

lazy val appName = "api-platform-events"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

inThisBuild(
  List(
    scalaVersion := "2.13.10",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val microservice = Project(appName, file("."))
  .settings(
    name := appName,
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.10",
    PlayKeys.playDefaultPort := 6700,
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= AppDependencies(),
    publishingSettings,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)

  .settings(ScoverageSettings())

  .settings(inConfig(Test)(BloopDefaults.configSettings))
  .settings(
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    Test / parallelExecution := false,
    Test / unmanagedSourceDirectories += baseDirectory.value / "testcommon",
    Test / unmanagedSourceDirectories += baseDirectory.value / "test"
  )

  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(BloopDefaults.configSettings))
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
      "uk.gov.hmrc.apiplatform.modules.applications.domain.models._",
      "uk.gov.hmrc.apiplatformevents.controllers.binders._"
    )
  )

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
