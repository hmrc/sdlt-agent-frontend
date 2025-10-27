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
import models.AgentDetails
import models.responses.SubmitAgentDetailsResponse
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class StampDutyLandTaxServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def newService(): (StampDutyLandTaxService, StampDutyLandTaxConnector) = {
    val connector = mock(classOf[StampDutyLandTaxConnector])
    val service = new StampDutyLandTaxService(connector)
    (service, connector)
  }

  val sorn = "SN001"
  
  val agentReferenceNumber = "ARN001"

  "getAgentDetails" should {
    "delegate to connector with the given storn and return the payload" in {

      val (service, connector) = newService()

      val payload = Some(AgentDetails(
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
      ))

      when(connector.getAgentDetails(eqTo(sorn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.getAgentDetails(sorn).futureValue
      result mustBe payload

      verify(connector).getAgentDetails(eqTo(sorn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.getAgentDetails(eqTo(sorn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getAgentDetails(sorn).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "removeAgentDetails" should {
    "delegate to connector with the given storn and return the payload" in {

      val (service, connector) = newService()

      val payload = true

      when(connector.removeAgentDetails(eqTo(sorn), eqTo(agentReferenceNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.removeAgentDetails(sorn, agentReferenceNumber).futureValue
      result mustBe payload

      verify(connector).removeAgentDetails(eqTo(sorn), eqTo(agentReferenceNumber))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }

    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.removeAgentDetails(eqTo(sorn), eqTo(agentReferenceNumber))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.removeAgentDetails(sorn, agentReferenceNumber).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "getAllAgentDetails" should {
    "delegate to connector with the given storn and return the payload" in {

      val (service, connector) = newService()

      val payload = List(
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

      when(connector.getAllAgentDetails(eqTo(sorn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(payload))

      val result = service.getAllAgentDetails(sorn).futureValue
      result mustBe payload

      verify(connector).getAllAgentDetails(eqTo(sorn))(any[HeaderCarrier])
      verifyNoMoreInteractions(connector)
    }
    "propagate failures from the connector" in {

      val (service, connector) = newService()

      when(connector.getAllAgentDetails(eqTo(sorn))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = intercept[RuntimeException] {
        service.getAllAgentDetails(sorn).futureValue
      }

      ex.getMessage must include("boom")
    }
  }

  "submitAgentDetails" should {

    val payload = AgentDetails(
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

    "delegate to connector with the given AgentDetails and return the success json" in {

      val (service, connector) = newService()

      val payload = AgentDetails(
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
}
