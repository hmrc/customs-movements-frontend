import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "customs-movements-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

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
  retrieveManaged := true,
  libraryDependencies ++= Dependencies.compile ++ Dependencies.test,
  TwirlKeys.templateImports ++= List.empty
)

lazy val scalacFlags = List(
  "-deprecation",                                // warn about use of deprecated APIs
  "-encoding", "UTF-8",                          // source files are in UTF-8
  "-feature",                                    // warn about misused language features
  "-language:implicitConversions",
  "-unchecked",                                  // warn about unchecked type parameters
  //"-Wconf:any:warning-verbose",
  "-Wconf:cat=unused-imports&src=routes/.*:s",   // silent "unused import" warnings from Play routes
  "-Wconf:cat=unused-imports&src=twirl/.*:is",   // silent "unused import" warnings from Twirl templates
  "-Wextra-implicit",
  "-Xcheckinit",
  "-Xfatal-warnings",                            // warnings are fatal!!
  "-Ywarn-numeric-widen",
  "-Wconf:cat=unused&src=.*routes.*:s", // silence private val defaultPrefix in class Routes is never used
  "-Wconf:msg=eq not selected from this instance:s", // silence eq not selected from this instance warning
  "-Wconf:msg=While parsing annotations in:s" // silence While parsing annotations in warning
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
