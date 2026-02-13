import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "customs-movements-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.7"

PlayKeys.devSettings := List("play.server.http.port" -> "6796")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(commonSettings)
  .settings(scoverageSettings)

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    publish / skip := true,
    Test / testOptions += Tests.Argument("-o", "-h", "it/target/html-report")
  )

lazy val commonSettings = List(
  scalacOptions ++= scalacFlags,
  scalacOptions := scalacOptions.value.distinct,
  retrieveManaged := true,
  libraryDependencies ++= Dependencies(),
  TwirlKeys.templateImports ++= List.empty
)


lazy val scalacFlags = List(
  "-feature",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Wconf:src=target/.*:s",
  "-Wconf:msg=eq not selected from this instance:s",
  "-Wconf:msg=While parsing annotations in:s"
)

// Prevent the "No processor claimed any of these annotations" warning
javacOptions ++= List("-Xlint:-processing")

lazy val scoverageSettings = List(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "metrics\\..*",
    "features\\..*",
    "test\\..*",
    ".*(BuildInfo|Routes|Options|TestingUtilitiesController).*"
  ).mkString(";"),
  coverageMinimumStmtTotal := 90,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)

addCommandAlias("ucomp", "Test/compile")
addCommandAlias("icomp", "it/Test/compile")
addCommandAlias("precommit", ";clean;scalafmt;Test/scalafmt;it/Test/scalafmt;coverage;test;it/test;scalafmtCheckAll;coverageReport")
