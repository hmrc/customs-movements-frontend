import play.api.Configuration
trait IleQueryDisabled extends IntegrationSpec {

  override def ileQueryFeatureConfiguration: Configuration =
    Configuration.from(Map("microservice.services.features.ileQuery" -> "disabled"))
}
