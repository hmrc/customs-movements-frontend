import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.20.0-play-26",
    "uk.gov.hmrc" %% "logback-json-logger" % "4.6.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.35.0-play-26",
    "uk.gov.hmrc" %% "play-health" % "3.14.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "7.40.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "8.4.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.41.0",
    "uk.gov.hmrc" %% "wco-dec" % "0.31.0",
    "uk.gov.hmrc" %% "play-language" % "3.4.0",
    "ai.x"         %% "play-json-extensions" % "0.30.1"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test",
    "org.jsoup" % "jsoup" % "1.11.3" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.mockito" % "mockito-core" % "2.27.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
