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

import connectors.AddressLookupConnector
import models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val sorn = "SN001"

  val agentReferenceNumber = "ARN001"

  "Init AddressLookup Journey" should {
    "return new Location on success" in {

      val connector = mock(classOf[AddressLookupConnector])
      val sessionRepository = mock(classOf[SessionRepository])
      val service = new AddressLookupService(connector, sessionRepository)

      val expectedPayload = JourneyInitSuccessResponse(Some("locationA"))

      // Setup
      when( connector.initJourney(eqTo(sorn))(any[HeaderCarrier]) )
        .thenReturn( Future.successful(Right(expectedPayload)))

      // Test
      val result = service.initJourney(sorn).futureValue
      result mustBe Right(expectedPayload)

      // Verification
      verify(connector).initJourney(eqTo(sorn))(any[HeaderCarrier])
    }
  }
}
