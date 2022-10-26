import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"    % "5.24.0",
    "uk.gov.hmrc"          %% "logback-json-logger"           % "5.2.0",
    "uk.gov.hmrc"          %% "play-allowlist-filter"         % "1.1.0",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"          %% "play-frontend-hmrc"            % "3.21.0-play-28",
    "uk.gov.hmrc"          %% "play-json-union-formatter"     % "1.16.0-play-28",
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-28"            % "0.68.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.13.3",
    "ai.x"                 %% "play-json-extensions"          % "0.42.0",
    "com.github.tototoshi" %% "scala-csv"                     % "1.3.10",
    "org.webjars.npm"      %  "govuk-frontend"                % "4.1.0",
    "org.webjars.npm"      %  "accessible-autocomplete"       % "2.0.4"
  )

  val test = Seq(
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current  % "test, it",
    "org.scalatest"          %% "scalatest"          % "3.2.12"             % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"              % "test, it",
    "org.mockito"            %% "mockito-scala"      % "1.17.12"            % "test",
    "com.vladsch.flexmark"   %  "flexmark-all"       % "0.62.2"             % "test, it",
    "org.jsoup"              %  "jsoup"              % "1.14.3"             % "test, it",
    "com.github.tomakehurst" %  "wiremock-jre8"      % "2.33.2"             % "test, it"
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}