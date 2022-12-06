import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.SbtAutoBuildPlugin
import bloop.integrations.sbt.BloopDefaults

lazy val root = (project in file("."))
  .settings(
    name := "api-platform-events",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.8",
    PlayKeys.playDefaultPort := 6700,
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies ++= AppDependencies(),
    publishingSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
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
    Keys.fork in IntegrationTest := false,
    IntegrationTest / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "testcommon",
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "it",
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((definedTests in IntegrationTest).value)
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
