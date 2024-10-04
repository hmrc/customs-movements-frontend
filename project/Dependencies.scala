import sbt.*

object Dependencies {

  val bootstrapPlayVersion = "9.2.0"
  val frontendPlayVersion = "10.7.0"
  val hmrcMongoVersion = "2.2.0"

  val compile: Seq[ModuleID] = List(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"            % bootstrapPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"            % frontendPlayVersion,
    "uk.gov.hmrc"           %% "play-conditional-form-mapping-play-30" % "3.1.0",
    "uk.gov.hmrc"           %% "play-json-union-formatter"             % "1.21.0",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.webjars.npm"       %  "accessible-autocomplete"               % "3.0.0",
    "commons-codec"         %  "commons-codec"                         % "1.17.1",
    "javax.xml.bind"        %  "jaxb-api"                              % "2.3.1"
  )

  val test: Seq[ModuleID] = List(
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"  % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30" % hmrcMongoVersion     % "test",
    "com.vladsch.flexmark"  %  "flexmark-all"            % "0.64.8"             % "test",
    "org.jsoup"             %  "jsoup"                   % "1.18.1"             % "test",
    "org.mockito"           %% "mockito-scala-scalatest" % "1.17.37"            % "test",
    "org.scalatest"         %% "scalatest"               % "3.2.19"             % "test",
  )

  def apply(): Seq[ModuleID] = (compile ++ test).map(_.withSources)
}