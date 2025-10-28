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
import models.UserAnswers
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import models.responses.addresslookup.JourneyInitResponse.{AddressLookupResponse, JourneyInitFailureResponse, JourneyInitSuccessResponse}
import models.responses.addresslookup.JourneyOutcomeResponse.UnexpectedGetStatusFailure
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.EitherValues
import pages.manageAgents.AgentAddressDetails
import play.api.http.Status.INTERNAL_SERVER_ERROR
import repositories.SessionRepository

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupServiceSpec extends AnyWordSpec
  with ScalaFutures
  with Matchers
  with EitherValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  trait Fixture {
    val sorn: String = "SN001"
    val id: String = "idToExtractAddress"
    val userId: String = "userId"
    val connector: AddressLookupConnector = mock(classOf[AddressLookupConnector])
    val sessionRepository: SessionRepository = mock(classOf[SessionRepository])
    val service: AddressLookupService = new AddressLookupService(connector, sessionRepository)

    val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)

    val expectedAddressDetails = JourneyResultAddressModel(
      "auditRef",
      Address(lines = Seq("First line of address"), postcode = Some("FX1N 7ZW"))
    )
  }

  "Init AddressLookup Journey" should {

    "return new Location on success" in new Fixture {
      val expectedPayload = JourneyInitSuccessResponse(Some("locationA"))

      when(connector.initJourney(eqTo(sorn))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(expectedPayload)))

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

    "return UserAnswer on success" in new Fixture {

      val updatedAnswers: UserAnswers = UserAnswers(id = userId)
        .set(AgentAddressDetails, expectedAddressDetails).toOption
        .get
        .copy(lastUpdated = instant)

      when(sessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Some(expectedAddressDetails))))

      val result: Either[Throwable, UserAnswers] = service.getJourneyOutcome(id, userId).futureValue
      result mustBe a[Either[Throwable, UserAnswers]]

      // align result to the same timeStamp in lastUpdate field
      val resultValue: UserAnswers = result.value.copy(lastUpdated = instant)
      resultValue must equal(updatedAnswers)

      verify(connector, times(1)).getJourneyOutcome(eqTo(id))(any[HeaderCarrier])
      verify(sessionRepository, times(1)).set(any())
    }

    "return AddressLookupConnector error" in new Fixture {
      val updatedAnswers: UserAnswers = UserAnswers(id = userId)
        .set(AgentAddressDetails, expectedAddressDetails).toOption
        .get

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful( Left(UnexpectedGetStatusFailure(INTERNAL_SERVER_ERROR)) ) )

      val result: Either[Throwable, UserAnswers] = service.getJourneyOutcome(id, userId).futureValue
      result must equal (Left(UnexpectedGetStatusFailure(INTERNAL_SERVER_ERROR)))

      verify(sessionRepository, times(0)).set(any())
      verify(connector, times(1)).getJourneyOutcome(any())(any[HeaderCarrier])
    }

    "return SessionRepository error" in new Fixture {

      val updatedAnswers: UserAnswers = UserAnswers(id = userId)
        .set(AgentAddressDetails, expectedAddressDetails).toOption
        .get

      when(sessionRepository.set(any()))
        .thenThrow(new RuntimeException("Failed connect to MongoDb | Or save user session"))

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Some(expectedAddressDetails))))

      assertThrows[RuntimeException] {
        service.getJourneyOutcome(id, userId).futureValue
      }

      verify(sessionRepository, times(1)).set(any())
      verify(connector, times(1)).getJourneyOutcome(any())(any[HeaderCarrier])
    }

  }
}
