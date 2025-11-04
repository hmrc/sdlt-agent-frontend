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

package utils.mangeAgents

import models.{AgentDetailsResponse, Mode, NormalMode}
import play.api.mvc.Call

trait AgentDetailsTestUtil {

  lazy val agentNameRequestRoute: String = controllers.manageAgents.routes.AgentNameController.onPageLoad(NormalMode).url
  
  val MAX_AGENTS = 25

  object AgentNamePageUtils {
    val agentNameOnwardRoute: Mode => Call = mode => controllers.manageAgents.routes.AddressLookupController.onPageLoad(mode)
    val agentNameDuplicateNameRoute: Mode => Call = mode => controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(mode)
  }
  
  object AgentNamePageWarningUtils {
    val WarningAgentNameRequestRoute: Mode => Call = mode => controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(mode)

    val onwardRoute: Mode => Call = mode =>  controllers.manageAgents.routes.AddressLookupController.onPageLoad(mode)
  }
  
  private def agent(i: Int): AgentDetailsResponse =
    AgentDetailsResponse(
      agentReferenceNumber = "ARN001",
      agentName            = s"Agent $i",
      houseNumber          = "64",
      addressLine1         = s"Address $i",
      addressLine2         = None,
      addressLine3         = "Lazy Town",
      addressLine4         = None,
      postcode             = Some("SW44GFS"),
      phone                = Some("0543534534543"),
      email                = "agent@example.com"
    )

  def getAgentList(n: Int): List[AgentDetailsResponse] = (1 to n).map(agent).toList

  val testAgentDetails: AgentDetailsResponse = AgentDetailsResponse(
    agentReferenceNumber = "ARN001",
    agentName            = "Harborview Estates",
    houseNumber          = "22A",
    addressLine1         = "Queensway",
    addressLine2         = None,
    addressLine3         = "Birmingham",
    addressLine4         = None,
    postcode             = Some("B2 4ND"),
    phone                = Some("01214567890"),
    email                = "info@harborviewestates.co.uk"
  )
}
