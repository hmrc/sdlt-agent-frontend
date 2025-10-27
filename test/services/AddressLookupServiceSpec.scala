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
import models.responses.addresslookup.JourneyInitResponse.{AddressLookupResponse, JourneyInitFailureResponse, JourneyInitSuccessResponse}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupServiceSpec extends AnyWordSpec with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  trait Fixture {
    val sorn: String = "SN001"
    val connector: AddressLookupConnector = mock(classOf[AddressLookupConnector])
    val sessionRepository: SessionRepository = mock(classOf[SessionRepository])
    val service: AddressLookupService = new AddressLookupService(connector, sessionRepository)
  }

  "Init AddressLookup Journey" should {

    "return new Location on success" in new Fixture {
      val expectedPayload = JourneyInitSuccessResponse(Some("locationA"))

      when( connector.initJourney(eqTo(sorn))(any[HeaderCarrier]) )
        .thenReturn( Future.successful(Right(expectedPayload)))

      val result: AddressLookupResponse = service.initJourney(sorn).futureValue
      result mustBe Right(expectedPayload)

      verify(connector).initJourney(eqTo(sorn))(any[HeaderCarrier])
    }

    "return failure response on error" in new Fixture {
      val expectedError = JourneyInitFailureResponse(INTERNAL_SERVER_ERROR)

      when(connector.initJourney(eqTo(sorn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(expectedError)))

      val result: AddressLookupResponse = service.initJourney(sorn).futureValue
      result mustBe Left(expectedError)

      verify(connector).initJourney(eqTo(sorn))(any[HeaderCarrier])
    }
  }

  "Get Address Lookup Journey result" should {

    "return UserAnswer on success" in {

      // Verification: Call getJourneyOutcome
      // Verification: Session Repository

    }

    // Error 1
    "return AddressLookupConnector error" in {

    }

    // Error 2
    "return SessionRepository error" in {
      
    }
    
  }
}
