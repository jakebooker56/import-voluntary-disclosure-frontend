/*
 * Copyright 2022 HM Revenue & Customs
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

package viewmodels.cya

import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}

object CYAHelper {

  def encodeMultilineText(content: Seq[String]): String = content.map(line => HtmlFormat.escape(line)).mkString("<br/>")

  def createRow(
    keyText: Content,
    valueContent: Content,
    action: Option[ActionItem] = None,
    columnClasses: String = "",
    rowClasses: String = ""
  ): SummaryListRow = {
    SummaryListRow(
      key = Key(content = keyText, classes = s"govuk-!-width-one-third $columnClasses".trim),
      value = Value(content = valueContent, classes = columnClasses),
      actions = action.map(act => Actions(items = Seq(act), classes = columnClasses)),
      classes = rowClasses
    )
  }

}
