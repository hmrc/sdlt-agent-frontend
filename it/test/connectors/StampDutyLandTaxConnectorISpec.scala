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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, post, stubFor, urlPathEqualTo}
import itutil.ApplicationWithWiremock
import models.{AgentDetailsResponse, AgentDetailsRequest}
import models.responses.SubmitAgentDetailsResponse
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.HeaderCarrier

class StampDutyLandTaxConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: StampDutyLandTaxConnector = app.injector.instanceOf[StampDutyLandTaxConnector]

  private val storn = "STN001"

  private val agentReferenceNumber = "ARN001"

  "getAgentDetails" should {

    val agentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/agent-details/get"

    "return AgentDetails when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(agentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .withQueryParam("agentReferenceNumber", equalTo(agentReferenceNumber))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "agentReferenceNumber": "ARN001",
                  |  "agentName": "Sunrise Realty",
                  |  "houseNumber": "8B",
                  |  "addressLine1": "Baker Street",
                  |  "addressLine3": "Manchester",
                  |  "postcode": "M1 2AB",
                  |  "phone": "01611234567",
                  |  "email": "contact@sunriserealty.co.uk"
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getAgentDetails(storn, agentReferenceNumber).futureValue
      result.map(_.agentName   ) mustBe Some("Sunrise Realty")
      result.map(_.houseNumber ) mustBe Some("8B")
      result.map(_.addressLine1) mustBe Some("Baker Street")
      result.map(_.addressLine3) mustBe Some("Manchester")
      result.flatMap(_.postcode) mustBe Some("M1 2AB")
      result.map(_.phone   ) mustBe Some("01611234567")
      result.map(_.email       ) mustBe Some("contact@sunriserealty.co.uk")
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
        connector.getAgentDetails(storn, agentReferenceNumber).futureValue
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
        connector.getAgentDetails(storn, agentReferenceNumber).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "getAllAgentDetails" should {

    val allAgentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/agent-details/get-all-agents"

    "return a list of AgentDetails when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """[
                  |  {
                  |    "agentReferenceNumber": "ARN001",
                  |    "agentName": "Acme Property Agents Ltd",
                  |    "houseNumber": "42",
                  |    "addressLine1": "High Street",
                  |    "addressLine2": "Westminster",
                  |    "addressLine3": "London",
                  |    "addressLine4": "Greater London",
                  |    "postcode": "SW1A 2AA",
                  |    "phone": "02079460000",
                  |    "email": "info@acmeagents.co.uk"
                  |  },
                  |  {
                  |    "agentReferenceNumber": "ARN002",
                  |    "agentName": "Harborview Estates",
                  |    "houseNumber": "22A",
                  |    "addressLine1": "Queensway",
                  |    "addressLine3": "Birmingham",
                  |    "postcode": "B2 4ND",
                  |    "phone": "01214567890",
                  |    "email": "info@harborviewestates.co.uk"
                  |  }
                  |]
                  |""".stripMargin
              )
          )
      )

      val expected = List(
        AgentDetailsResponse(
          agentReferenceNumber = "ARN001",
          agentName = "Acme Property Agents Ltd",
          houseNumber = "42",
          addressLine1 = "High Street",
          addressLine2 = Some("Westminster"),
          addressLine3 = "London",
          addressLine4 = Some("Greater London"),
          postcode = Some("SW1A 2AA"),
          phone = "02079460000",
          email = "info@acmeagents.co.uk"
        ),
        AgentDetailsResponse(
          agentReferenceNumber = "ARN002",
          agentName = "Harborview Estates",
          houseNumber = "22A",
          addressLine1 = "Queensway",
          addressLine2 = None,
          addressLine3 = "Birmingham",
          addressLine4 = None,
          postcode = Some("B2 4ND"),
          phone = "01214567890",
          email = "info@harborviewestates.co.uk"
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

    val agentDetails = AgentDetailsRequest(
      agentName = "Acme Property Agents Ltd",
      houseNumber = "42",
      addressLine1 = "High Street",
      addressLine2 = Some("Westminster"),
      addressLine3 = "London",
      addressLine4 = Some("Greater London"),
      postcode = Some("SW1A 2AA"),
      phone = "02079460000",
      email = "info@acmeagents.co.uk"
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

  "removeAgentDetails" should {

    val removeAgentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/agent-details/remove"

    "return true when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(removeAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .withQueryParam("agentReferenceNumber", equalTo(agentReferenceNumber))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                Json.stringify(JsBoolean(true))
              )
          )
      )

      val result = connector.removeAgentDetails(storn, agentReferenceNumber).futureValue

      result mustBe true
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo(removeAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .withQueryParam("agentReferenceNumber", equalTo(agentReferenceNumber))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.removeAgentDetails(storn, agentReferenceNumber).futureValue
      }
      ex.getMessage.toLowerCase must include("jsboolean")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo(removeAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .withQueryParam("agentReferenceNumber", equalTo(agentReferenceNumber))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[Exception] {
        connector.removeAgentDetails(storn, agentReferenceNumber).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

}
