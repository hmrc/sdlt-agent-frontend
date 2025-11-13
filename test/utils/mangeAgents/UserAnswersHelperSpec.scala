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

import base.SpecBase
import models.{AgentDetailsResponse, UserAnswers}
import models.manageAgents.AgentContactDetails
import models.requests.DataRequest
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import org.mockito.Mockito.*
import org.scalatest.TryValues
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.{AgentAddressPage, AgentContactDetailsPage, AgentNamePage}
import utils.manageAgents.UserAnswersHelper

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

      val be = AgentDetailsResponse(
        agentReferenceNumber = testArn,
        agentName = "Harborview Estates",
        addressLine1 = "42 Queensway",
        addressLine2 = None,
        addressLine3 = Some("Birmingham"),
        addressLine4 = None,
        postcode = Some("B2 4ND"),
        phone = Some("01214567890"),
        email = Some("info@harborviewestates.co.uk")
      )

      implicit val dr: DataRequest[_] = mockDataRequest(startUa)

      val updated = Helper.updateUserAnswers(be).success.value

      updated.get(AgentNamePage).value mustBe "Harborview Estates"

      val addr: JourneyResultAddressModel = updated.get(AgentAddressPage).value
      addr.auditRef mustBe "" // helper hardcodes empty auditRef
      addr.address mustBe Address(
        lines    = Seq("Queensway", "", "Birmingham", ""),
        postcode = Some("B2 4ND")
      )

      updated.get(AgentContactDetailsPage).value mustBe
        AgentContactDetails(phone = Some("01214567890"), email = Some("info@harborviewestates.co.uk"))
    }

    "should populate Address when all address lines are present" in {
      val startUa = emptyUserAnswersWithStorn

      val be = AgentDetailsResponse(
        agentReferenceNumber = testArn,
        agentName = "Sunrise Realty",
        addressLine1 = "8B Baker Street",
        addressLine2 = Some("Marylebone"),
        addressLine3 = Some("London"),
        addressLine4 = Some("Greater London"),
        postcode = Some("NW1 6XE"),
        phone = Some("02071234567"),
        email = Some("contact@sunriserealty.co.uk")
      )

      implicit val dr: DataRequest[_] = mockDataRequest(startUa)

      val updated = Helper.updateUserAnswers(be).success.value

      updated.get(AgentNamePage).value mustBe "Sunrise Realty"

      val addr: JourneyResultAddressModel = updated.get(AgentAddressPage).value
      addr.address mustBe Address(
        lines    = Seq("Baker Street", "Marylebone", "London", "Greater London"),
        postcode = Some("NW1 6XE")
      )

      updated.get(AgentContactDetailsPage).value mustBe
        AgentContactDetails(phone = Some("02071234567"), email = Some("contact@sunriserealty.co.uk"))
    }

    "should handle missing phone by writing None for phone and Some(email) for email" in {
      val startUa = emptyUserAnswersWithStorn

      val be = AgentDetailsResponse(
        agentReferenceNumber = testArn,
        agentName = "Willow Properties",
        addressLine1 = "14 High Street",
        addressLine2 = None,
        addressLine3 = Some("Manchester"),
        addressLine4 = None,
        postcode = Some("M1 2AB"),
        phone = None,
        email = Some("hello@willow.co.uk")
      )

      implicit val dr: DataRequest[_] = mockDataRequest(startUa)

      val updated = Helper.updateUserAnswers(be).success.value

      updated.get(AgentContactDetailsPage).value mustBe
        AgentContactDetails(phone = None, email = Some("hello@willow.co.uk"))
    }
  }
}
