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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import models.responses.addresslookup.JourneyInitResponse.{AddressLookupResponse, JourneyInitFailureResponse, JourneyInitSuccessResponse}
import models.responses.addresslookup.JourneyOutcomeResponse.UnexpectedGetStatusFailure
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, argThat, eq as eqTo}
import org.scalatest.EitherValues
import pages.manageAgents.AgentAddressPage
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.i18n.Messages
import repositories.SessionRepository

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}
import play.api.test.Helpers.stubMessages
import org.scalactic.TripleEquals.*
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest


class AddressLookupServiceSpec extends AnyWordSpec
  with ScalaFutures
  with Matchers
  with EitherValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val messages: Messages = stubMessages()
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: RequestHeader = FakeRequest("GET", "/dummy-url")

  trait Fixture {
    val storn: String = "SN001"
    val id: String = "idToExtractAddress"
    val userId: String = "userId"
    val userAnswer = UserAnswers(userId)

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
      Seq(NormalMode, CheckMode).foreach { mode =>
        val expectedPayload = JourneyInitSuccessResponse(Some("locationA"))
        when(
          connector.initJourney(any(), any())(
            any[HeaderCarrier],
            any[Messages],
            any[RequestHeader]
          )
        ).thenReturn(Future.successful(Right(expectedPayload)))

        val result: AddressLookupResponse =
          service.initJourney(userAnswer, storn, mode).futureValue

        result mustBe Right(expectedPayload)

        verify(connector, times(1)).initJourney(
          any(),
          eqTo(mode)
        )(
          any[HeaderCarrier],
          any[Messages],
          any[RequestHeader]
        )
      }
    }

    "return failure response on error" in new Fixture {
      Seq(NormalMode, CheckMode).foreach { mode =>
        val expectedError = JourneyInitFailureResponse(INTERNAL_SERVER_ERROR)

        when(
          connector.initJourney(any(), any())(
            any[HeaderCarrier],
            any[Messages],
            any[RequestHeader]
          )
        ).thenReturn(Future.successful(Left(expectedError)))

        val result: AddressLookupResponse =
          service.initJourney(userAnswer, storn, mode).futureValue

        result mustBe Left(expectedError)

        verify(connector, times(1)).initJourney(
          any(),
          eqTo(mode)
        )(
          any[HeaderCarrier],
          any[Messages],
          any[RequestHeader]
        )
      }
    }

  }

  "Get Address Lookup Journey result" should {

    "return UserAnswer on success" in new Fixture {

      val updatedAnswers: UserAnswers = UserAnswers(id = userId)
        .set(AgentAddressPage, expectedAddressDetails).toOption
        .get
        .copy(lastUpdated = instant)

      when(sessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Some(expectedAddressDetails))))

      val result: Either[Throwable, UserAnswers] = service.getJourneyOutcome(id, userAnswer).futureValue
      result mustBe a[Either[Throwable, UserAnswers]]

      // align result to the same timeStamp in lastUpdate field
      val resultValue: UserAnswers = result.value.copy(lastUpdated = instant)
      resultValue must equal(updatedAnswers)

      verify(connector, times(1)).getJourneyOutcome(eqTo(id))(any[HeaderCarrier])
      verify(sessionRepository, times(1)).set(any())
    }

    "return AddressLookupConnector error" in new Fixture {
      val updatedAnswers: UserAnswers = UserAnswers(id = userId)
        .set(AgentAddressPage, expectedAddressDetails).toOption
        .get

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(UnexpectedGetStatusFailure(INTERNAL_SERVER_ERROR))))

      val result: Either[Throwable, UserAnswers] = service.getJourneyOutcome(id, userAnswer).futureValue
      result must equal(Left(UnexpectedGetStatusFailure(INTERNAL_SERVER_ERROR)))

      verify(sessionRepository, times(0)).set(eqTo(updatedAnswers))
      verify(connector, times(1)).getJourneyOutcome(any())(any[HeaderCarrier])
    }

    "return SessionRepository error" in new Fixture {

      val updatedAnswers: UserAnswers = UserAnswers(id = userId)
        .set(AgentAddressPage, expectedAddressDetails).toOption
        .get
        .copy(lastUpdated = instant)

      when(sessionRepository.set(eqTo(updatedAnswers)))
        .thenThrow(new RuntimeException("Failed connect to MongoDb | Or save user session"))

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Some(expectedAddressDetails))))

      assertThrows[RuntimeException] {
        service.getJourneyOutcome(id, userAnswer).futureValue
      }

      verify(connector, times(1)).getJourneyOutcome(any())(any[HeaderCarrier])

      // User custom equality / override last updated
      verify(sessionRepository, times(1)).set( argThat(captureUserAnswer =>
        captureUserAnswer.copy(lastUpdated = instant) === updatedAnswers
      ) )
    }

    "failed to save AddressDetails as None extracted" in new Fixture {

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(None)))

      val _ = service.getJourneyOutcome(id, userAnswer).futureValue

      verify(connector, times(1)).getJourneyOutcome(any())(any[HeaderCarrier])
    }

    // TODO: required trait for UserAnswer class to abstract away methods/-> mock UserAnswer
    "failed to set UserAnswer while attempt to save" in new Fixture {

      val updatedAnswersMaybe: Option[UserAnswers] = None

      when(connector.getJourneyOutcome(eqTo(id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(updatedAnswersMaybe)))

      val _ = service.getJourneyOutcome(id, UserAnswers("")).futureValue

      verify(connector, times(1)).getJourneyOutcome(any())(any[HeaderCarrier])
    }

  }

}
