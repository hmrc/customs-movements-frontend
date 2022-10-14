import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.{ForkedJvmPerTestSettings, SbtAutoBuildPlugin}

val appName = "customs-movements-frontend"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6796")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin, SbtWeb)
  .settings(
    libraryDependencies ++= (AppDependencies.compile ++ AppDependencies.test),
    dependencyOverrides += "commons-codec" % "commons-codec" % "1.15",
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    majorVersion := 0,
    scalaVersion := "2.12.12"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(publishingSettings: _*)
  .settings(
    Test / unmanagedSourceDirectories := Seq((Test / baseDirectory).value / "test/unit", (Test / baseDirectory).value / "test/util"),
    addTestReportOption(Test, "test-reports")
  )
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := Seq(
      (IntegrationTest / baseDirectory).value / "test/it",
      (Test / baseDirectory).value / "test/util"
    ),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := ForkedJvmPerTestSettings.oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false
  )
  .settings(
    // concatenate js - https://www.npmjs.com/package/concatenate-js-middleware
    Concat.groups :=
      Seq(
        "stylesheets/vendor/jquery-ui.min.css" -> group(Seq("stylesheets/vendor/jquery-ui.css")),
        "javascripts/customsdecexfrontend-app.js" -> group(Seq("javascripts/customsdecexfrontend.js"))
      ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("customsdecexfrontend-*.js")
  )
  .settings(scoverageSettings)
  .settings(silencerSettings)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "metrics\\..*",
    "features\\..*",
    "test\\..*",
    ".*(BuildInfo|Routes|Options|TestingUtilitiesController).*"
  ).mkString(";"),
  coverageMinimumStmtTotal := 84,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)

lazy val silencerSettings: Seq[Setting[_]] = {
  val silencerVersion = "1.7.0"
  Seq(
    libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full)),
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )
}
