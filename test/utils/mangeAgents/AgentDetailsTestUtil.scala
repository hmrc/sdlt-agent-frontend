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

import models.responses.organisation.CreatedAgent
import models.{Mode, NormalMode}
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

  private def agent(i: Int): CreatedAgent =
    CreatedAgent(
      storn = "STN001",
      agentId = None,
      name = s"Agent $i",
      houseNumber = None,
      address1 = s"64 Address $i",
      address2 = None,
      address3 = Some("Lazy Town"),
      address4 = None,
      postcode = Some("SW44GFS"),
      phone = Some("01214567890"),
      email = Some("info@harborviewestates.co.uk"),
      dxAddress = None,
      agentResourceReference = "ARN001"
    )

  def getAgentList(n: Int): List[CreatedAgent] = (1 to n).map(agent).toList

  val testAgentDetails: CreatedAgent = CreatedAgent(
    storn = "STN001",
    agentId = None,
    name = "Harborview Estates",
    houseNumber = None,
    address1 = "22A Queensway",
    address2 = None,
    address3 = Some("Birmingham"),
    address4 = None,
    postcode = Some("B2 4ND"),
    phone = Some("01214567890"),
    email = Some("info@harborviewestates.co.uk"),
    dxAddress = None,
    agentResourceReference = "ARN001"
  )
}
