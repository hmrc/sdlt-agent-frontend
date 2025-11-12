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

package utils.manageAgents

import models.AgentDetailsResponse
import models.manageAgents.AgentContactDetails
import models.requests.DataRequest
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import pages.manageAgents.{AgentAddressPage, AgentContactDetailsPage, AgentNameDuplicateWarningPage, AgentNamePage}

trait UserAnswersHelper {

  def updateUserAnswers(agentDetails: AgentDetailsResponse)(implicit request: DataRequest[_]) = {
    for {
      userAnswersOne <- request.userAnswers.remove(AgentNameDuplicateWarningPage)
      userAnswersTwo <- userAnswersOne.set(AgentNamePage, agentDetails.agentName)
      addressLines = Seq(agentDetails.addressLine1, agentDetails.addressLine2.getOrElse(""), agentDetails.addressLine3.getOrElse(""), agentDetails.addressLine4.getOrElse(""))
      userAnswersThree <- userAnswersTwo.set(AgentAddressPage, JourneyResultAddressModel("", Address(addressLines, agentDetails.postcode)))
      userAnswersFour <- userAnswersThree.set(AgentContactDetailsPage, AgentContactDetails(agentDetails.phone, agentDetails.email))
    } yield userAnswersFour
  }
}
