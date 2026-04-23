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
import models.manageAgents.AgentContactDetails
import models.{CheckMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.manageAgents.AgentContactDetailsPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.manageAgents.checkAnswers.AgentContactDetailsSummary

class AgentContactDetailsSummarySpec extends SpecBase with Matchers {

  "AgentContactDetailsSummary.row" - {
    "when userAnswer is empty" - {
      "must not return SummaryListRow" in new Setup {
        val summaryListRow: Option[SummaryListRow] = AgentContactDetailsSummary.row(emptyUserAnswers)
        summaryListRow mustBe None
      }
    }

    "when userAnswer is populated" - {
      "must render the correct key in SummaryListRow" in new Setup {
        val summaryListRow: Option[SummaryListRow] = AgentContactDetailsSummary.row(populatedUserAnswers)

        val keyInSummaryListRow: Key = summaryListRow.value.key

        keyInSummaryListRow.content.asHtml.toString must equal(actualKey)

      }

      "must render correct value when email and phone both are defined in SummaryListRow" in new Setup {
        val userAnswerWithBothPhoneAndEmail: UserAnswers = createUserAnswersWithEmailAndPhone(
          populatedUserAnswers,
          Some(phone),
          Some(email)
        )
        val summaryListRow: Option[SummaryListRow] = AgentContactDetailsSummary.row(userAnswerWithBothPhoneAndEmail)

        val actualValue: String =s"""
             |$telephoneKey: $phone<br>
             |$emailKey: $email
             |""".stripMargin

        val summaryListRowValue: Value = summaryListRow.value.value

        summaryListRowValue.content.asHtml.toString must equal(actualValue)

      }

      "must render only email when phone is not defined in SummaryListRow" in new Setup {

        val userAnswerWithOnlyEmail: UserAnswers = createUserAnswersWithEmailAndPhone(
          populatedUserAnswers,
          None,
          Some(email)
        )
        val summaryListRow: Option[SummaryListRow] = AgentContactDetailsSummary.row(userAnswerWithOnlyEmail)

        val actualValue: String =
          s"""
             |$telephoneKey: ${""}<br>
             |$emailKey: $email
             |""".stripMargin

        val summaryListRowValue: Value = summaryListRow.value.value

        summaryListRowValue.content.asHtml.toString must equal(actualValue)

      }

      "must render only phone in SummaryListRow value when email is not defined in SummaryListRow" in new Setup {

        val userAnswerWithOnlyPhone: UserAnswers = createUserAnswersWithEmailAndPhone(
          populatedUserAnswers,
          Some(phone),
          None
        )
        val summaryListRow: Option[SummaryListRow] = AgentContactDetailsSummary.row(userAnswerWithOnlyPhone)

        val actualValue: String =
          s"""
             |$telephoneKey: $phone<br>
             |$emailKey: ${""}
             |""".stripMargin

        val summaryListRowValue: Value = summaryListRow.value.value

        summaryListRowValue.content.asHtml.toString must equal(actualValue)

      }

      "must render correct content and navigate to correct action in SummaryListRow" in new Setup {

        val summaryListRow: Option[SummaryListRow] = AgentContactDetailsSummary.row(populatedUserAnswers)

        val actionInSummaryListRow: Option[Actions] = summaryListRow.value.actions

        val actionItem: ActionItem = actionInSummaryListRow.value.items.head

        actionItem.content.asHtml.toString must equal(siteChangeKey)

        actionItem.href must equal(route)

      }

    }
  }

  trait Setup {

    implicit val messages: Messages = stubMessages()

    def createUserAnswersWithEmailAndPhone(userAnswers: UserAnswers, phone: Option[String] = None, email: Option[String] = None): UserAnswers = {
      val testAgentContactDetails = AgentContactDetails(phone, email)
      userAnswers.set(AgentContactDetailsPage, testAgentContactDetails).success.value
    }

    val email = "john@example.com"
    val phone = "07123456789"

    val actualKey: String = messages("manageAgents.agentContactDetailsSummary.checkYourAnswersLabel")
    val emailKey: String = messages("manageAgents.agentContactDetailsSummary.value.email")
    val telephoneKey: String = messages("manageAgents.agentContactDetailsSummary.value.telephone")

    val siteChangeKey: String = messages("site.change")
    val route: String = controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(CheckMode).url

  }

}
