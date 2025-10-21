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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, post, stubFor, urlPathEqualTo, urlPathMatching}
import itutil.ApplicationWithWiremock
import models.AgentDetails
import models.responses.SubmitAgentDetailsResponse
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import uk.gov.hmrc.http.HeaderCarrier

class StampDutyLandTaxConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: StampDutyLandTaxConnector = app.injector.instanceOf[StampDutyLandTaxConnector]

  private val storn = "STN001"

  "getAgentDetails" should {

    val agentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/agent-details/storn/$storn"

    "return AgentDetails when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(agentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "storn": "STN001",
                  |  "name": "Sunrise Realty",
                  |  "houseNumber": "8B",
                  |  "addressLine1": "Baker Street",
                  |  "addressLine3": "Manchester",
                  |  "postcode": "M1 2AB",
                  |  "phoneNumber": "01611234567",
                  |  "emailAddress": "contact@sunriserealty.co.uk",
                  |  "agentId": "3454354325",
                  |  "isAuthorised": 1
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getAgentDetails(storn).futureValue
      result.storn        mustBe "STN001"
      result.name         mustBe "Sunrise Realty"
      result.houseNumber  mustBe "8B"
      result.addressLine1 mustBe "Baker Street"
      result.addressLine3 mustBe "Manchester"
      result.postcode     mustBe Some("M1 2AB")
      result.phoneNumber  mustBe "01611234567"
      result.emailAddress mustBe "contact@sunriserealty.co.uk"
      result.agentId      mustBe "3454354325"
      result.isAuthorised mustBe 1
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo(agentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getAgentDetails(storn).futureValue
      }
      ex.getMessage.toLowerCase must include("storn")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo(agentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.getAgentDetails(storn).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "getAllAgentDetails" should {

    val allAgentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/agent-details/get-all-agents/storn/$storn"

    "return a list of AgentDetails when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """[
                  |  {
                  |    "storn": "STN001",
                  |    "name": "Acme Property Agents Ltd",
                  |    "houseNumber": "42",
                  |    "addressLine1": "High Street",
                  |    "addressLine2": "Westminster",
                  |    "addressLine3": "London",
                  |    "addressLine4": "Greater London",
                  |    "postcode": "SW1A 2AA",
                  |    "phoneNumber": "02079460000",
                  |    "emailAddress": "info@acmeagents.co.uk",
                  |    "agentId": "AGT001",
                  |    "isAuthorised": 1
                  |  },
                  |  {
                  |    "storn": "STN001",
                  |    "name": "Harborview Estates",
                  |    "houseNumber": "22A",
                  |    "addressLine1": "Queensway",
                  |    "addressLine3": "Birmingham",
                  |    "postcode": "B2 4ND",
                  |    "phoneNumber": "01214567890",
                  |    "emailAddress": "info@harborviewestates.co.uk",
                  |    "agentId": "AGT001",
                  |    "isAuthorised": 1
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val expected = List(
        AgentDetails(
          storn = "STN001",
          name = "Acme Property Agents Ltd",
          houseNumber = "42",
          addressLine1 = "High Street",
          addressLine2 = Some("Westminster"),
          addressLine3 = "London",
          addressLine4 = Some("Greater London"),
          postcode = Some("SW1A 2AA"),
          phoneNumber = "02079460000",
          emailAddress = "info@acmeagents.co.uk",
          agentId = "AGT001",
          isAuthorised = 1
        ),
        AgentDetails(
          storn = "STN001",
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
      )

      val result = connector.getAllAgentDetails(storn).futureValue

      result mustBe expected
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllAgentDetails(storn).futureValue
      }
      ex.getMessage.toLowerCase must include("storn")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.getAllAgentDetails(storn).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "submitAgentDetails" should {

    val submitAgentDetailsUrl = "/stamp-duty-land-tax/manage-agents/agent-details/submit"

    val agentDetails = AgentDetails(
      storn = "STN001",
      name = "Acme Property Agents Ltd",
      houseNumber = "42",
      addressLine1 = "High Street",
      addressLine2 = Some("Westminster"),
      addressLine3 = "London",
      addressLine4 = Some("Greater London"),
      postcode = Some("SW1A 2AA"),
      phoneNumber = "02079460000",
      emailAddress = "info@acmeagents.co.uk",
      agentId = "AGT001",
      isAuthorised = 1
    )

    "return SubmitAgentDetailsResponse when BE returns 200 with valid JSON" in {
      stubFor(
        post(urlPathEqualTo(submitAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "agentResourceRef": "ARN4324234" }""")
          )
      )

      val result = connector.submitAgentDetails(agentDetails).futureValue

      result mustBe SubmitAgentDetailsResponse(agentResourceRef = "ARN4324234")
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        post(urlPathEqualTo(submitAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.submitAgentDetails(agentDetails).futureValue
      }
      ex.getMessage must include("agentResourceRef")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        post(urlPathEqualTo(submitAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.submitAgentDetails(agentDetails).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }
}
