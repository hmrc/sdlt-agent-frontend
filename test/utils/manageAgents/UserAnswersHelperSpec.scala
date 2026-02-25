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

import base.SpecBase
import models.UserAnswers
import models.manageAgents.AgentContactDetails
import models.requests.DataRequest
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import models.responses.organisation.CreatedAgent
import org.mockito.Mockito.*
import org.scalatest.TryValues
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.{AgentAddressPage, AgentContactDetailsPage, AgentNamePage}

class UserAnswersHelperSpec
  extends SpecBase
    with MockitoSugar
    with TryValues {

  object Helper extends UserAnswersHelper

  private def mockDataRequest(userAnswers: UserAnswers): DataRequest[_] = {
    val req = mock[DataRequest[_]]
    when(req.userAnswers).thenReturn(userAnswers)
    req
  }

  "UserAnswersHelper.updateUserAnswers" - {

    "should populate AgentName, Address (with missing optional lines), and ContactDetails" in {
      val startUa = emptyUserAnswersWithStorn

      val be: CreatedAgent =
        CreatedAgent(
          storn = testStorn,
          agentId = None,
          name = "Harborview Estates",
          houseNumber = None,
          address1 = "42 Queensway",
          address2 = None,
          address3 = Some("Birmingham"),
          address4 = None,
          postcode = Some("B2 4ND"),
          phone = Some("01214567890"),
          email = Some("info@harborviewestates.co.uk"),
          dxAddress = None,
          agentResourceReference = testArn
        )


      implicit val dr: DataRequest[_] = mockDataRequest(startUa)

      val updated = Helper.updateUserAnswers(be).success.value

      updated.get(AgentNamePage).value mustBe "Harborview Estates"

      val addr: JourneyResultAddressModel = updated.get(AgentAddressPage).value
      addr.auditRef mustBe "" // helper hardcodes empty auditRef
      addr.address mustBe Address(
        lines    = Seq("42 Queensway", "", "Birmingham", ""),
        postcode = Some("B2 4ND")
      )

      updated.get(AgentContactDetailsPage).value mustBe
        AgentContactDetails(phone = Some("01214567890"), email = Some("info@harborviewestates.co.uk"))
    }

    "should populate Address when all address lines are present" in {
      val startUa = emptyUserAnswersWithStorn

      val be: CreatedAgent =
        CreatedAgent(
          storn = testStorn,
          agentId = None,
          name = "Sunrise Realty",
          houseNumber = None,
          address1 = "8B Baker Street",
          address2 = Some("Marylebone"),
          address3 = Some("London"),
          address4 = Some("Greater London"),
          postcode = Some("NW1 6XE"),
          phone = Some("01214567890"),
          email = Some("info@harborviewestates.co.uk"),
          dxAddress = None,
          agentResourceReference = testArn
        )

      implicit val dr: DataRequest[_] = mockDataRequest(startUa)

      val updated = Helper.updateUserAnswers(be).success.value

      updated.get(AgentNamePage).value mustBe "Sunrise Realty"

      val addr: JourneyResultAddressModel = updated.get(AgentAddressPage).value
      addr.address mustBe Address(
        lines    = Seq("8B Baker Street", "Marylebone", "London", "Greater London"),
        postcode = Some("NW1 6XE")
      )

      updated.get(AgentContactDetailsPage).value mustBe
        AgentContactDetails(phone = Some("01214567890"), email = Some("info@harborviewestates.co.uk"))
    }
  }
}
