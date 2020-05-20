import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % "1.2.0-play-26",
    "uk.gov.hmrc"          %% "logback-json-logger"           % "4.6.0",
    "uk.gov.hmrc"          %% "govuk-template"                % "5.48.0-play-26",
    "uk.gov.hmrc"          %% "play-health"                   % "3.14.0-play-26",
    "uk.gov.hmrc"          %% "play-ui"                       % "8.5.0-play-26",
    "uk.gov.hmrc"          %% "bootstrap-play-26"             % "1.3.0",
    "uk.gov.hmrc"          %% "play-frontend-govuk"           % "0.32.0-play-26",
    "org.webjars.npm"      %  "govuk-frontend"                % "3.4.0",
    "ai.x"                 %% "play-json-extensions"          % "0.40.2",
    "uk.gov.hmrc"          %% "play-whitelist-filter"         % "3.1.0-play-26",
    "uk.gov.hmrc"          %% "simple-reactivemongo"          % "7.22.0-play-26",
    "uk.gov.hmrc"          %% "play-json-union-formatter"     % "1.7.0",
    "com.github.tototoshi" %% "scala-csv"                     % "1.3.6",
    "com.github.cloudyrock.mongock"  %  "mongock-core"        % "2.0.2",
    "org.mongodb"          %  "mongo-java-driver"             % "3.12.1",
    "org.webjars.npm"      %  "hmrc-frontend"                 % "1.5.0"
  )

  val test = Seq(
    "org.scalatest"          %% "scalatest"          % "3.0.8"             % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"             % "test, it",
    "org.pegdown"            %  "pegdown"            % "1.6.0"             % "test, it",
    "org.jsoup"              %  "jsoup"              % "1.12.1"            % "test, it",
    "com.github.tomakehurst" %  "wiremock-jre8"      % "2.24.1"            % "test, it",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % "test, it",
    "org.mockito"            %  "mockito-core"       % "3.0.0"             % "test"
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}
