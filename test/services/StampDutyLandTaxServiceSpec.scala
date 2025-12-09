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

package services

import connectors.StampDutyLandTaxConnector
import models.requests.{CreatePredefinedAgentRequest, UpdatePredefinedAgent}
import models.responses.{CreatePredefinedAgentResponse, DeletePredefinedAgentResponse, UpdatePredefinedAgentResponse}
import models.requests.DeletePredefinedAgentRequest
import models.responses.organisation.{CreatedAgent, SdltOrganisationResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()


  private def newService(): (StampDutyLandTaxService, StampDutyLandTaxConnector) = {
    val connector = mock(classOf[StampDutyLandTaxConnector])
    val service = new StampDutyLandTaxService(connector)
    (service, connector)
  }

  val storn = "SN001"
  val agentReferenceNumber = "ARN001"

  val payload: SdltOrganisationResponse =
    SdltOrganisationResponse(
      storn = "STN001",
      version = Some("1"),
      isReturnUser = Some("Y"),
      doNotDisplayWelcomePage = Some("N"),
      agents = Seq(
        CreatedAgent(
          storn = "STN001",
          agentId = None,
          name = "Smith & Co Solicitors",
          houseNumber = None,
          address1 = "12 High Street",
          address2 = Some("London"),
          address3 = Some("Greater London"),
          address4 = None,
          postcode = Some("SW1A 1AA"),
          phone = Some("01214567890"),
          email = Some("info@harborviewestates.co.uk"),
          dxAddress = None,
          agentResourceReference = "ARN001"
        ),
        CreatedAgent(
          storn = "STN001",
          agentId = None,
          name = "Anderson Legal LLP",
          houseNumber = None,
          address1 = "45B Baker Street",
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = Some("NW1 6XE"),
          phone = Some("01214567890"),
          email = Some("info@harborviewestates.co.uk"),
          dxAddress = None,
          agentResourceReference = "ARN002"
        )
      )
    )


  "deletePredefinedAgent" should {

    val deletePredefinedAgentRequest = DeletePredefinedAgentRequest(storn, agentReferenceNumber)
    val deletePredefinedAgentResponse = DeletePredefinedAgentResponse(true)

    "delegate to connector with the given valid DeletePredefinedAgentRequest and return the payload" in {

      val (service, connector) = newService()

      when(connector.deletePredefinedAgent(eqTo(deletePredefinedAgentRequest))(any[HeaderCarrier]))
        .thenReturn(Future(deletePredefinedAgentResponse))

      val result = service.deletePredefinedAgent(deletePredefinedAgentRequest).futureValue
      result mustBe deletePredefinedAgentResponse

      verify(connector).deletePredefinedAgent(eqTo(deletePredefinedAgentRequest))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.deletePredefinedAgent(eqTo(deletePredefinedAgentRequest))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.deletePredefinedAgent(deletePredefinedAgentRequest).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "getAllAgentDetails" should {
    "delegate to connector with the given storn and return the payload" in {

      val (service, connector) = newService()

      when(connector.getSdltOrganisation(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.getAllAgentDetails(storn).futureValue
      result mustBe payload.agents

      verify(connector).getSdltOrganisation(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.getSdltOrganisation(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getAllAgentDetails(storn).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "submitAgentDetails" should {

    val payload = CreatePredefinedAgentRequest(
      storn = "STNOO1",
      agentName = "42 Acme Property Agents Ltd",
      addressLine1 = Some("High Street"),
      addressLine2 = Some("Westminster"),
      addressLine3 = Some("London"),
      addressLine4 = Some("Greater London"),
      postcode = Some("SW1A 2AA"),
      phone = Some("02079460000"),
      email = Some("info@acmeagents.co.uk")
    )

    "delegate to connector with the given AgentDetails and return the success json" in {

      val (service, connector) = newService()

      val response = CreatePredefinedAgentResponse(agentResourceRef = "ARN-001", agentId = "1234")

      when(connector.submitAgentDetails(eqTo(payload))(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      val result = service.submitAgentDetails(payload).futureValue
      result mustBe response

      verify(connector).submitAgentDetails(eqTo(payload))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.submitAgentDetails(eqTo(payload))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.submitAgentDetails(payload).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "updateAgentDetails" should {

    val payload = UpdatePredefinedAgent(
      agentResourceReference = Some("ARN001"),
      storn = "STNOO1",
      houseNumber = None,
      agentName = "42 Acme Property Agents Ltd",
      addressLine1 = Some("High Street"),
      addressLine2 = Some("Westminster"),
      addressLine3 = Some("London"),
      addressLine4 = Some("Greater London"),
      postcode = Some("SW1A 2AA"),
      phone = Some("02079460000"),
      email = Some("info@acmeagents.co.uk"),
      dxAddress = None
    )

    "delegate to connector with the given AgentDetails" in {

      val (service, connector) = newService()

      when(connector.updateAgentDetails(eqTo(payload))(any[HeaderCarrier]))
        .thenReturn(Future(UpdatePredefinedAgentResponse(true)))

      val result = service.updateAgentDetails(payload).futureValue
      result mustBe UpdatePredefinedAgentResponse(true)

      verify(connector).updateAgentDetails(eqTo(payload))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.updateAgentDetails(eqTo(payload))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.updateAgentDetails(payload).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "isDuplicate" should {
    "return true when the agent name already exists and there is a duplicate" in {
      val (service, connector) = newService()
      
      when(connector.getSdltOrganisation(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.isDuplicate(storn, "Smith & Co Solicitors").futureValue

      result mustBe true

      verify(connector).getSdltOrganisation(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "return false when the agent name does not exist" in {
      val (service, connector) = newService()

      when(connector.getSdltOrganisation(eqTo(storn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.isDuplicate(storn, "Nonexistent Agent").futureValue

      result mustBe false

      verify(connector).getSdltOrganisation(eqTo(storn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
  }
}
