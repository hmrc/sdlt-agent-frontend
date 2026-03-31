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

package services

import controllers.routes.NoSessionDataController
import models.UserAnswers
import pages.manageAgents.{AgentAddressPage, AgentNamePage}
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.manageAgents.checkAnswers.{AddressSummary, AgentNameSummary, ContactEmailSummary, ContactPhoneNumberSummary}

import javax.inject.{Inject, Singleton}

@Singleton
class CheckYourAnswersService @Inject() {
  
  def validateUserAnswers(userAnswers: UserAnswers): Either[Result, Unit] = {
    val agentName = userAnswers.get(AgentNamePage)
    val agentAddress = userAnswers.get(AgentAddressPage)

    (agentName, agentAddress) match {
      case (Some(agentName), Some(agentAddress)) =>
        Right(())
      case _ =>
        Left(Redirect(NoSessionDataController.onPageLoad()))
    }
  }

  def getSummaryListRows(userAnswers: UserAnswers)(implicit messages: Messages): SummaryList = SummaryListViewModel(
    rows = Seq(
      AgentNameSummary.row(userAnswers),
      AddressSummary.row(userAnswers),
      ContactPhoneNumberSummary.row(userAnswers),
      ContactEmailSummary.row(userAnswers)
    ).flatten
  )

}
