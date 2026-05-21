import sbt.*

object Dependencies {

  val bootstrapPlayVersion = "10.7.0"
  val frontendPlayVersion = "13.7.0"
  val hmrcMongoVersion = "2.12.0"

  val compile: Seq[ModuleID] = List(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"            % bootstrapPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"            % frontendPlayVersion,
    "uk.gov.hmrc"           %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "uk.gov.hmrc"           %% "play-json-union-formatter"             % "1.24.0",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.webjars.npm"       %  "accessible-autocomplete"               % "3.0.1",
    "commons-codec"         %  "commons-codec"                         % "1.22.0",
    "jakarta.xml.bind"      %  "jakarta.xml.bind-api"                  % "4.0.5"
  )

  val test: Seq[ModuleID] = List(
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"  % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30" % hmrcMongoVersion     % "test",
    "com.vladsch.flexmark"  %  "flexmark-all"            % "0.64.8"             % "test",
    "org.jsoup"             %  "jsoup"                   % "1.22.2"             % "test",
    "org.scalatestplus"     %% "mockito-5-12"            % "3.2.19.0"           % "test",
    "org.scalatest"         %% "scalatest"               % "3.2.20"             % "test",
  )

  private val missingSources = List("accessible-autocomplete", "flexmark-all")

  def apply(): Seq[ModuleID] =
    (compile ++ test).map(moduleId => if (missingSources.contains(moduleId.name)) moduleId else moduleId.withSources)
}
