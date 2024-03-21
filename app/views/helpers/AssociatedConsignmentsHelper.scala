/*
 * Copyright 2024 HM Revenue & Customs
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

package views.helpers

import models.notifications.queries.{DucrInfo, MucrInfo, UcrInfo}
import models.viewmodels.ilequery.IleQueryCodeConverter
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Empty, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import controllers.ileQuery.routes.IleQueryController
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import views.html.components.gds.link
object AssociatedConsignmentsHelper {
  def generateRowsForChildUcrs(childUcrs: Seq[UcrInfo])(implicit messages: Messages, converter: IleQueryCodeConverter): Seq[Seq[TableRow]] = {
    val link = new link

    def generateRow(info: UcrInfo, idx: Int, mucrOrDucrSoeConversion: String => Content): Seq[TableRow] =
      Seq(
        TableRow(
          content = HtmlContent(link(Html(info.ucr), None, IleQueryController.getConsignmentData(info.ucr))),
          attributes = Map("id" -> s"associateUcr_${idx}_ucr")
        ),
        TableRow(
          content = info.entryStatus.flatMap(_.roe).map(converter.routeOfEntry(_)).getOrElse(Empty),
          attributes = Map("id" -> s"associateUcr_${idx}_roe")
        ),
        TableRow(
          content = info.entryStatus.flatMap(_.soe).map(mucrOrDucrSoeConversion).getOrElse(Empty),
          attributes = Map("id" -> s"associateUcr_${idx}_soe")
        )
      )
    childUcrs.zipWithIndex.map {
      case (info: DucrInfo, idx) => generateRow(info, idx, converter.statusOfEntryDucr)
      case (info: MucrInfo, idx) => generateRow(info, idx, converter.statusOfEntryMucr)
    }
  }
}
