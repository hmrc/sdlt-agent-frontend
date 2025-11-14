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
import models.{AgentDetailsRequest, AgentDetailsResponse}
import models.responses.SubmitAgentDetailsResponse
import models.responses.organisation.SdltOrganisationResponse
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
      version = 1,
      isReturnUser = "Y",
      doNotDisplayWelcomePage = "N",
      agents = Seq(
        AgentDetailsResponse(
          agentName = "Smith & Co Solicitors",
          addressLine1 = "12 High Street",
          addressLine2 = Some("London"),
          addressLine3 = Some("Greater London"),
          addressLine4 = None,
          postcode = Some("SW1A 1AA"),
          phone = "02071234567",
          email = "info@smithco.co.uk",
          agentReferenceNumber = "ARN001"
        ),
        AgentDetailsResponse(
          agentName = "Anderson Legal LLP",
          addressLine1 = "45B Baker Street",
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postcode = Some("NW1 6XE"),
          phone = "02077644567",
          email = "contact@andersonlegal.com",
          agentReferenceNumber = "ARN002"
        )
      )
    )

  "removeAgentDetails" should {
    "delegate to connector with the given storn and return the payload" in {

      val (service, connector) = newService()

      val payload = true

      when(connector.removeAgentDetails(eqTo(storn), eqTo(agentReferenceNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.removeAgentDetails(storn, agentReferenceNumber).futureValue
      result mustBe payload

      verify(connector).removeAgentDetails(eqTo(storn), eqTo(agentReferenceNumber))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.removeAgentDetails(eqTo(storn), eqTo(agentReferenceNumber))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.removeAgentDetails(storn, agentReferenceNumber).futureValue
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

    val payload = AgentDetailsRequest(
      agentName = "42 Acme Property Agents Ltd",
      addressLine1 = Some("High Street"),
      addressLine2 = Some("Westminster"),
      addressLine3 = Some("London"),
      addressLine4 = Some("Greater London"),
      postcode = Some("SW1A 2AA"),
      phone = "02079460000",
      email = "info@acmeagents.co.uk"
    )

    "delegate to connector with the given AgentDetails and return the success json" in {

      val (service, connector) = newService()

      val response = SubmitAgentDetailsResponse(agentResourceRef = "ARN-001")

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
