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

import models.UserAnswers
import models.manageAgents.AgentContactDetails
import models.requests.DataRequest
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import models.responses.organisation.CreatedAgent
import pages.manageAgents.{AgentAddressPage, AgentContactDetailsPage, AgentNameDuplicateWarningPage, AgentNamePage, AgentReferenceNumberPage}
import play.api.mvc.AnyContent

import scala.util.Try

trait UserAnswersHelper {

  // Try to convert createdAgent object to UserAnswers
  def updateUserAnswers(agentDetails: CreatedAgent)
                       (implicit request: DataRequest[_]): Try[UserAnswers] = {
    for {
      userAnswersOne <- request.userAnswers.remove(AgentNameDuplicateWarningPage)
      userAnswersTwo <- userAnswersOne.set(AgentNamePage, agentDetails.name)
      addressLines = Seq(agentDetails.address1, agentDetails.address2.getOrElse(""), agentDetails.address3.getOrElse(""), agentDetails.address4.getOrElse(""))
      userAnswersThree <- userAnswersTwo.set(AgentAddressPage, JourneyResultAddressModel("", Address(addressLines, agentDetails.postcode)))
      userAnswersFour <- userAnswersThree.set(AgentContactDetailsPage, AgentContactDetails(agentDetails.phone, agentDetails.email))
      userAnswersFive <- userAnswersFour.set(AgentReferenceNumberPage, agentDetails.agentResourceReference)
    } yield userAnswersFive
  }

  // Attempt to extract agentName from request.userAnswer
  def getAgentName(implicit request: DataRequest[AnyContent]): Either[Throwable, String] =
    request.userAnswers.get(AgentNamePage) match {
      case Some(name) =>
        Right(name)
      case None =>
        Left(Error("Couldn't find agent in user answers"))
    }

}
