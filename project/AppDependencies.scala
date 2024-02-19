import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.3.0"
  val frontendPlayVersion = "8.5.0"
  val hmrcMongoVersion = "1.7.0"
  val jacksonVersion = "2.14.2"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"                       %% "bootstrap-frontend-play-30"               % bootstrapPlayVersion,
    "uk.gov.hmrc"                       %% "play-frontend-hmrc-play-30"               % frontendPlayVersion,
    "uk.gov.hmrc.mongo"                 %% "hmrc-mongo-play-30"                       % hmrcMongoVersion,
    "com.fasterxml.jackson.module"      %% "jackson-module-scala"                     % jacksonVersion,
    "uk.gov.hmrc"                       %% "play-allowlist-filter"                    % "1.2.0",
    "uk.gov.hmrc"                       %% "play-conditional-form-mapping-play-30"    % "2.0.0",
    "uk.gov.hmrc"                       %% "play-json-union-formatter"                % "1.20.0",
    "com.github.tototoshi"              %% "scala-csv"                                % "1.3.10",
    "org.webjars.npm"                   %  "accessible-autocomplete"                  % "2.0.4",
    "org.webjars.npm"                   %  "govuk-frontend"                           % "4.7.0",
    "commons-codec"                     %  "commons-codec"                            % "1.15",
    "javax.xml.bind"                    %  "jaxb-api"                                 % "2.3.1"
  )

  val testScope = "test,it"

  val test = Seq(
    "uk.gov.hmrc"                      %% "bootstrap-test-play-30"         % bootstrapPlayVersion % testScope,
    "uk.gov.hmrc.mongo"                %% "hmrc-mongo-test-play-30"        % hmrcMongoVersion     % testScope,
    "com.vladsch.flexmark"             %  "flexmark-all"                   % "0.64.6"             % testScope,
    "org.jsoup"                        %  "jsoup"                          % "1.15.4"             % "test",
    "org.mockito"                      %% "mockito-scala-scalatest"        % "1.17.29"            % "test",
    "org.scalatest"                    %% "scalatest"                      % "3.2.15"             % testScope,
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}