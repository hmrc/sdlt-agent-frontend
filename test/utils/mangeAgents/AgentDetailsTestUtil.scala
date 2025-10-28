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

import models.AgentDetails

trait AgentDetailsTestUtil {

  val testStorn: String = "STN001"
  
  val MAX_AGENTS = 25

  private def agent(i: Int): AgentDetails =
    AgentDetails(
      storn         = "STN001",
      name          = s"Agent $i",
      houseNumber   = "64",
      addressLine1  = s"Address $i",
      addressLine2  = None,
      addressLine3  = "Lazy Town",
      addressLine4  = None,
      postcode      = Some("SW44GFS"),
      phoneNumber   = "0543534534543",
      emailAddress  = "agent@example.com",
      agentId       = "AN001",
      isAuthorised  = 1
    )

  def getAgentList(n: Int): List[AgentDetails] = (1 to n).map(agent).toList

  val testAgentDetails: AgentDetails = AgentDetails(
    storn = "STN001",
    agentReferenceNumber = Some("ARN001"),
    name = "Harborview Estates",
    houseNumber = "22A",
    addressLine1 = "Queensway",
    addressLine2 = None,
    addressLine3 = "Birmingham",
    addressLine4 = None,
    postcode = Some("B2 4ND"),
    phoneNumber = "01214567890",
    emailAddress = "info@harborviewestates.co.uk",
    agentId = "AGT001",
    isAuthorised = 1
  )
}
