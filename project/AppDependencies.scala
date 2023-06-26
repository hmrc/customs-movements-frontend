import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "7.12.0"
  val hmrcMongoVersion = "0.74.0"
  val jacksonVersion = "2.14.1"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28"    % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "uk.gov.hmrc"                  %% "play-allowlist-filter"         % "1.1.0",
    "uk.gov.hmrc"                  %% "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"            % "5.3.0-play-28",
    "uk.gov.hmrc"                  %% "play-json-union-formatter"     % "1.17.0-play-28",
    "com.typesafe.play"            %% "play-json"                     % "2.9.3",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"          % "2.14.1",
    "ai.x"                         %% "play-json-extensions"          % "0.42.0",
    "com.github.tototoshi"         %% "scala-csv"                     % "1.3.10",
    "org.webjars.npm"              %  "govuk-frontend"                % "4.4.1",
    "org.webjars.npm"              %  "accessible-autocomplete"       % "2.0.4",
    "commons-codec"                %  "commons-codec"                 % "1.15"
  )

  val testScope = "test,it"

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion     % testScope,
    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.64.0"             % testScope,
    "org.jsoup"              %  "jsoup"                   % "1.15.3"             % "test",
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.12"            % "test",
    "org.scalatest"          %% "scalatest"               % "3.2.14"             % testScope,
    "com.github.tomakehurst" %  "wiremock-jre8"           % "2.35.0"             % testScope
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}