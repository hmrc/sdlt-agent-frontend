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

package utils.manageAgents

import base.SpecBase
import models.UserAnswers
import models.manageAgents.AgentContactDetails
import org.scalatest.matchers.must.Matchers
import pages.manageAgents.{
  AgentAddressPage,
  AgentContactDetailsPage,
  AgentNamePage
}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.manageAgents.CheckYourAnswersHelper.*

class CheckYourAnswersHelperSpec extends Matchers with SpecBase {

  "getSummaryListRows" - {

    "include all rows when all answers are present" in new Setup {
      val result: SummaryList =
        CheckYourAnswersHelper.getSummaryListRows(populatedUserAnswers)
      result.rows.length mustBe 4
    }

    "include all rows when optional answers are empty" in new Setup {
      val ua: UserAnswers = UserAnswers("id")
        .set(AgentNamePage, "Haborview Estates")
        .success
        .value
        .set(AgentAddressPage, testAgentAddress)
        .success
        .value
        .set(AgentContactDetailsPage, AgentContactDetails(None, None))
        .success
        .value

      val result: SummaryList = CheckYourAnswersHelper.getSummaryListRows(ua)
      result.rows.length mustBe 4
    }

    "include all rows when AgentContactDetailsPage is defined" in new Setup {

      val userAnswersWithAgentContactDetailsPage: UserAnswers =
        populatedUserAnswers
          .set(AgentContactDetailsPage, testAgentContactDetails)
          .success
          .value

      val summaryList: SummaryList = CheckYourAnswersHelper.getSummaryListRows(
        userAnswersWithAgentContactDetailsPage
      )

      summaryList.rows.length mustBe 4
    }

    "exclude AgentContactDetailsSummary when AgentContactDetailsPage is not defined" in new Setup {

      val userAnswersWithOutAgentContactDetailsPage: UserAnswers =
        UserAnswers("id")
          .set(AgentNamePage, "Haborview Estates")
          .success
          .value
          .set(AgentAddressPage, testAgentAddress)
          .success
          .value

      val summaryList: SummaryList = CheckYourAnswersHelper.getSummaryListRows(
        userAnswersWithOutAgentContactDetailsPage
      )

      summaryList.rows.length mustBe 3

    }

  }

  "validateUserAnswers" - {

    "returns Right when required fields exist" in new Setup {
      val result: Either[Result, SummaryList] =
        validateUserAnswers(validUserAnswers)

      result.isRight mustBe true
    }

    "redirect to NoSessionDataController when AgentName is missing" in new Setup {
      val ua: UserAnswers = UserAnswers("id")
        .set(AgentAddressPage, testAgentAddress)
        .success
        .value
      val result: Either[Result, SummaryList] = validateUserAnswers(ua)

      result mustBe Left(Redirect(noSessionDataUrl))
    }

    "redirect to NoSessionDataController when AgentAddress is missing" in new Setup {
      val ua: UserAnswers = UserAnswers("id")
        .set(AgentNamePage, "Haborview Estates")
        .success
        .value
      val result: Either[Result, SummaryList] = validateUserAnswers(ua)

      result mustBe Left(Redirect(noSessionDataUrl))
    }

    "redirect to NoSessionDataController when both answers are missing" in new Setup {
      val result: Either[Result, SummaryList] =
        validateUserAnswers(emptyUserAnswersWithStorn)

      result mustBe Left(Redirect(noSessionDataUrl))
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val testAgentContactDetails: AgentContactDetails =
      AgentContactDetails(Some("phone"), Some("email"))
    implicit val messages: Messages = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
    val noSessionDataUrl: String =
      controllers.routes.NoSessionDataController.onPageLoad().url
  }
}
