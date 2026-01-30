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

package models.responses.organisation

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CreatedAgentSpec extends AnyWordSpec with Matchers {

  def generateCreatedAgent(inputAddress2: Option[String]): CreatedAgent =
    CreatedAgent(
      storn = "STNOO1",
      agentId = Some("123"),
      name = "John Doe",
      houseNumber = None,
      address1 = "4",
      address2 = inputAddress2,
      address3 = None,
      address4 = None,
      postcode = None,
      phone = Some("0234567898"),
      email = Some("test@email.com"),
      dxAddress = None,
      agentResourceReference = "56745"
    )

  "getAddressLine1AndAddressLine2 in CreatedAgent " should {

    "concatenate and return address1 and address2" when {

      "address2 is defined" in {

        val address2 = "Winchester Road"

        val createdAgentWithAddressLine2 = generateCreatedAgent(Some(address2))

        val combinedAddressLine1AndAddressLine2 =
          s"${createdAgentWithAddressLine2.address1} ${address2}"

        createdAgentWithAddressLine2.getAddressLine1AndAddressLine2 mustBe combinedAddressLine1AndAddressLine2
      }

      "address2 is not defined" in {

        val createdAgentWithoutAddressLine2 = generateCreatedAgent(None)

        val combinedAddressLine1AndAddressLine2 =
          s"${createdAgentWithoutAddressLine2.address1}"

        createdAgentWithoutAddressLine2.getAddressLine1AndAddressLine2 mustBe combinedAddressLine1AndAddressLine2
      }
    }
  }

}
