import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.1.0-play-26",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.20.0-play-26",
    "uk.gov.hmrc" %% "logback-json-logger" % "4.6.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.38.0-play-26",
    "uk.gov.hmrc" %% "play-health" % "3.14.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.0.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "8.5.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.46.0",
    "uk.gov.hmrc" %% "wco-dec" % "0.31.0",
    "uk.gov.hmrc" %% "play-language" % "4.1.0",
    "uk.gov.hmrc" %% "play-whitelist-filter" % "3.1.0-play-26",
    "ai.x"         %% "play-json-extensions" % "0.40.2",
    "com.github.tototoshi" %% "scala-csv" % "1.3.6"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test",
    "org.jsoup" % "jsoup" % "1.12.1" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.mockito" % "mockito-core" % "3.0.0" % "test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
  }
