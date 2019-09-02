/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package filters

import akka.stream.Materializer
import com.google.inject.{ProvidedBy, Provider}
import javax.inject.Inject
import play.api.Configuration
import play.api.mvc.{Call, RequestHeader, Result}
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

import scala.concurrent.Future

@ProvidedBy(classOf[WhitelistIpFilterProvider])
class WhitelistIpFilter(
  val enabled: Boolean,
  val excludeList: Seq[String],
  override val whitelist: Seq[String],
  override val mat: Materializer
) extends AkamaiWhitelistFilter {

  override val destination: Call = Call("GET", "https://www.gov.uk")

  override val excludedPaths: Seq[Call] = excludeList.map(Call("GET", _))

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] =
    if (enabled) {
      super.apply(f)(rh)
    } else {
      f(rh)
    }
}

class WhitelistIpFilterProvider @Inject()(configuration: Configuration, materializer: Materializer)
    extends Provider[WhitelistIpFilter] {
  override def get(): WhitelistIpFilter = {
    val whitelistConfig = configuration.get[Configuration]("ip-whitelist")
    val enabled = whitelistConfig.get[Boolean]("enabled")
    val exclude = whitelistConfig.get[Seq[String]]("exclude")
    val list = whitelistConfig.get[Seq[String]]("list")
    new WhitelistIpFilter(enabled, exclude, list, materializer)
  }
}
