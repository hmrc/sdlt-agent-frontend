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
import models.requests.CreatePredefinedAgentRequest
import models.responses.CreatePredefinedAgentResponse
import models.responses.organisation.{CreatedAgent, SdltOrganisationResponse}
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

  "getSdltOrganisation" should {

    val allAgentDetailsUrl = s"/stamp-duty-land-tax/manage-agents/get-sdlt-organisation"

    "return a list of AgentDetails when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(allAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "storn": "STN001",
                  |  "version": "1",
                  |  "isReturnUser": "Y",
                  |  "doNotDisplayWelcomePage": "N",
                  |  "agents": [
                  |    {
                  |      "storn": "STN001",
                  |      "agentId": null,
                  |      "name": "42 Acme Property Agents Ltd",
                  |      "houseNumber": null,
                  |      "address1": "High Street",
                  |      "address2": "Westminster",
                  |      "address3": "London",
                  |      "address4": "Greater London",
                  |      "postcode": "SW1A 2AA",
                  |      "phone": "02079460000",
                  |      "email": "info@acmeagents.co.uk",
                  |      "dxAddress": null,
                  |      "agentResourceReference": "ARN001"
                  |    },
                  |    {
                  |      "storn": "STN001",
                  |      "agentId": null,
                  |      "name": "Harborview Estates",
                  |      "houseNumber": null,
                  |      "address1": "22A Queensway",
                  |      "address2": null,
                  |      "address3": "Birmingham",
                  |      "address4": null,
                  |      "postcode": "B2 4ND",
                  |      "phone": "01214567890",
                  |      "email": "info@harborviewestates.co.uk",
                  |      "dxAddress": null,
                  |      "agentResourceReference": "ARN002"
                  |    }
                  |  ]
                  |}
                  |""".stripMargin
              )
          )
      )

      val expected =
        SdltOrganisationResponse(
          storn = "STN001",
          version = Some("1"),
          isReturnUser = Some("Y"),
          doNotDisplayWelcomePage = Some("N"),
          agents = Seq(
            CreatedAgent(
              storn = "STN001",
              agentId = None,
              name = "42 Acme Property Agents Ltd",
              houseNumber = None,
              address1 = "High Street",
              address2 = Some("Westminster"),
              address3 = Some("London"),
              address4 = Some("Greater London"),
              postcode = Some("SW1A 2AA"),
              phone = "02079460000",
              email = "info@acmeagents.co.uk",
              dxAddress = None,
              agentResourceReference = "ARN001"
            ),
            CreatedAgent(
              storn = "STN001",
              agentId = None,
              name = "Harborview Estates",
              houseNumber = None,
              address1 = "22A Queensway",
              address2 = None,
              address3 = Some("Birmingham"),
              address4 = None,
              postcode = Some("B2 4ND"),
              phone = "01214567890",
              email = "info@harborviewestates.co.uk",
              dxAddress = None,
              agentResourceReference = "ARN002"
            )
          )
        )


      val result = connector.getSdltOrganisation(storn).futureValue

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
        connector.getSdltOrganisation(storn).futureValue
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
        connector.getSdltOrganisation(storn).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "submitAgentDetails" should {

    val submitAgentDetailsUrl = "/stamp-duty-land-tax/create/predefined-agent"

    val agentDetails = CreatePredefinedAgentRequest(
      storn = "STN001",
      agentName = "Acme Property Agents Ltd",
      addressLine1 = Some("42 High Street"),
      addressLine2 = Some("Westminster"),
      addressLine3 = Some("London"),
      addressLine4 = Some("Greater London"),
      postcode = Some("SW1A 2AA"),
      phone = Some("02079460000"),
      email = Some("info@acmeagents.co.uk")
    )

    "return CreatePredefinedAgentResponse when BE returns 200 with valid JSON" in {
      stubFor(
        post(urlPathEqualTo(submitAgentDetailsUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "agentResourceRef": "ARN4324234", "agentId" : "1234" }""")
          )
      )

      val result = connector.submitAgentDetails(agentDetails).futureValue

      result mustBe CreatePredefinedAgentResponse(agentResourceRef = "ARN4324234", agentId = "1234")
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

    "return Unit when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo(removeAgentDetailsUrl))
          .withQueryParam("storn", equalTo(storn))
          .withQueryParam("agentReferenceNumber", equalTo(agentReferenceNumber))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                ("""{ "message": "Agent with reference number ARN001 deleted for user with storn STN001" }""")
              )
          )
      )

      val result: Unit = connector.removeAgentDetails(storn, agentReferenceNumber).futureValue

      result mustBe ()
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
      ex.getMessage must include("boom")
    }
  }

}
