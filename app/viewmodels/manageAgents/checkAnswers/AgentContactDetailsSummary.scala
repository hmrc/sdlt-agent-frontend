/*
 * Copyright 2026 HM Revenue & Customs
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

import models.{CheckMode, UserAnswers}
import pages.manageAgents.AgentContactDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AgentContactDetailsSummary {

  def row(
      answers: UserAnswers
  )(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(AgentContactDetailsPage).map { agentContactDetails =>
      SummaryListRowViewModel(
        key = messages(
          "manageAgents.agentContactDetailsSummary.checkYourAnswersLabel"
        ),
        value = ValueViewModel(
          HtmlContent(
            s"""
               |${messages(
                "manageAgents.agentContactDetailsSummary.value.telephone"
              )}: ${HtmlFormat
                .escape(agentContactDetails.phone.getOrElse(""))
                .toString}<br>
               |${messages(
                "manageAgents.agentContactDetailsSummary.value.email"
              )}: ${HtmlFormat
                .escape(agentContactDetails.email.getOrElse(""))
                .toString}
               |""".stripMargin
          )
        ),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.manageAgents.routes.AgentContactDetailsController
              .onPageLoad(CheckMode)
              .url
          )
            .withVisuallyHiddenText(
              messages(s"manageAgents.agentContactDetailsSummary.change.hidden")
            )
            .withAttribute("id" -> "change-agent-contact-details")
        )
      )
    }
  }

}
