import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "7.20.0"
  val hmrcMongoVersion = "1.3.0"
  val jacksonVersion = "2.14.2"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28"    % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "uk.gov.hmrc"                  %% "play-allowlist-filter"         % "1.2.0",
    "uk.gov.hmrc"                  %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"            % "7.16.0-play-28",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"     % "1.18.0-play-28",
    "com.typesafe.play"            %% "play-json"                     % "2.9.4",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"          % "2.14.2",
    "ai.x"                         %% "play-json-extensions"          % "0.42.0",
    "com.github.tototoshi"         %% "scala-csv"                     % "1.3.10",
    "org.webjars.npm"              %  "govuk-frontend"                % "4.5.0",
    "org.webjars.npm"              %  "accessible-autocomplete"       % "2.0.4",
    "commons-codec"                %  "commons-codec"                 % "1.15"
  )

  val testScope = "test,it"

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion     % testScope,
    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.64.6"             % testScope,
    "org.jsoup"              %  "jsoup"                   % "1.15.4"             % "test",
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.12"            % "test",
    "org.scalatest"          %% "scalatest"               % "3.2.15"             % testScope,
    "com.github.tomakehurst" %  "wiremock-jre8"           % "2.35.0"             % testScope
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}