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

package viewmodels.govuk

import base.SpecBase
import models.{CheckMode, UserAnswers}
import models.manageAgents.AgentContactDetails
import org.scalatest.matchers.must.Matchers
import pages.manageAgents.{AgentContactDetailsPage, AgentNamePage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, SummaryListRow}
import viewmodels.manageAgents.checkAnswers.AddContactDetailsYesNoSummary

class AddContactDetailsYesNoSummarySpec extends SpecBase with Matchers {

  "AddContactDetailsYesNoSummary.row" - {
    "when user answer is empty" - {
      "must not return SummaryListRow" in new Setup {
        val summaryListRow: Option[SummaryListRow] = AddContactDetailsYesNoSummary.row(emptyUserAnswers)
        summaryListRow mustBe None
      }
    }

    "when user answer is populated" - {
      "must render agent name in the key of SummaryListRow" in new Setup {
        val summaryListRow: Option[SummaryListRow] = AddContactDetailsYesNoSummary.row(populatedUserAnswers)

        val keyValue: String = messages("manageAgents.addContactDetailsYesNoSummary.checkYourAnswersLabel", agentName)

        val row: SummaryListRow = summaryListRow.value

        row.key.content.asHtml.toString must equal(keyValue)
      }

      "must return row with value `Yes` when a user answers Yes and AgentContactDetailsPage is defined" in new Setup {

        val testAgentContactDetails: AgentContactDetails = AgentContactDetails(Some("123456789"), Some("test@email.com"))

        val userAnswersWithAddContactDetailsYesNoPage: UserAnswers = populatedUserAnswers
          .set(AgentContactDetailsPage, testAgentContactDetails).success.value

        val summaryListRow: Option[SummaryListRow] = AddContactDetailsYesNoSummary.row(userAnswersWithAddContactDetailsYesNoPage)

        summaryListRow.value.value.content.asHtml.toString must equal(yesValueKey)
      }
      "must return row with value `No` when user answers No and AgentContactDetailsPage is not defined" in new Setup {
        val userAnswersWithoutAddContactDetailsYesNoPage: UserAnswers = populatedUserAnswers.remove(AgentContactDetailsPage).success.value

        val summaryListRow: Option[SummaryListRow] = AddContactDetailsYesNoSummary.row(userAnswersWithoutAddContactDetailsYesNoPage)

        summaryListRow.value.value.content.asHtml.toString must equal(noValueKey)
      }
      "must render correct content and navigate to correct action in SummaryListRow" in new Setup {

        val summaryListRow: Option[SummaryListRow] = AddContactDetailsYesNoSummary.row(populatedUserAnswers)

        val actionInSummaryListRow: Option[Actions] = summaryListRow.value.actions

        val actionItem: ActionItem = actionInSummaryListRow.value.items.head

        actionItem.content.asHtml.toString must equal(siteChangeKey)

        actionItem.href must equal(route)

      }
    }
  }

  trait Setup {
    lazy implicit val messages: Messages = stubMessages()
    
    val agentName: String = populatedUserAnswers.get(AgentNamePage).getOrElse("")
    val yesValueKey: String = messages("manageAgents.agentContactDetailsSummary.value.yes")
    val noValueKey: String = messages("manageAgents.agentContactDetailsSummary.value.no")

    val siteChangeKey: String = messages("site.change")
    val route: String = controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(CheckMode).url
  }

}
