/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.manageAgents.checkAnswers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AnswerSummary {

  def row(answer: String, tag: String)(implicit messages: Messages): Option[SummaryListRow] = {
    val attribute = s"change-confirm-${tag.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase}"

    Some(
      SummaryListRowViewModel(
        key = messages(s"checkYourAnswers.$tag.label"),
        value = ValueViewModel(messages(answer)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            "http://localhost:10911/stamp-duty-land-tax-agent"
          )
            .withVisuallyHiddenText(messages(s"checkYourAnswers.$tag.change.hidden"))
            .withAttribute("id" -> attribute)
        )
      )
    )
  }
}


