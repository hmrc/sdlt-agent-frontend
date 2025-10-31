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


import config.FrontendAppConfig
import itutil.ApplicationWithWiremock
import mocks.MockHttpV2
import models.responses.addresslookup.JourneyInitResponse.{JourneyInitFailureResponse, JourneyInitSuccessResponse}
import models.responses.addresslookup.JourneyOutcomeResponse.UnexpectedGetStatusFailure
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.reset
import play.api.test.Helpers.stubMessages

import scala.concurrent.ExecutionContext

class AddressLookupConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock
  with MockHttpV2
  with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = stubMessages()
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  private val agentName: Option[String] = Some("agentName")
  private val errorId = "errorId"
  private val id = "someId"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient)
    reset(mockRequestBuilder)
  }

  object TestAddressLookupConnector extends AddressLookupConnector(appConfig, mockHttpClient, messagesApi)

  "Initiate Address Lookup Journey" should {
    "return location details on success" in {
      setupMockHttpPost(TestAddressLookupConnector.addressLookupInitializeUrl)(
        Right(JourneyInitSuccessResponse(Some("Some location")))
      )
      val result = TestAddressLookupConnector.initJourney(agentName).futureValue
      result mustBe Right(JourneyInitSuccessResponse(Some("Some location")))
    }
    "return failure when attempt init Journey" in {
      setupMockHttpPost(TestAddressLookupConnector.addressLookupInitializeUrl)(
        Left(JourneyInitFailureResponse(INTERNAL_SERVER_ERROR))
      )
      val result = TestAddressLookupConnector.initJourney(agentName).futureValue
      result mustBe Left(JourneyInitFailureResponse(INTERNAL_SERVER_ERROR))
    }
  }

  "Extract Address Details upon Journey completion" should {
    "return address details on success" in {

      val expectedJourneyResultAddressModel =
        JourneyResultAddressModel(auditRef = "auditRef",
          address = Address(lines = Seq.empty, postcode = Some("Z9 3WW"))
        )

      setupMockHttpGet(TestAddressLookupConnector.addressLookupOutcomeUrl(id))(
        Right(Some(expectedJourneyResultAddressModel))
      )
      val result = TestAddressLookupConnector.getJourneyOutcome(id).futureValue
      result mustBe Right(Some(expectedJourneyResultAddressModel))
    }
    "return error details on failure" in {
      setupMockHttpGet(TestAddressLookupConnector.addressLookupOutcomeUrl(errorId))(
        Left(UnexpectedGetStatusFailure(INTERNAL_SERVER_ERROR))
      )
      val result = TestAddressLookupConnector.getJourneyOutcome(errorId).futureValue
      result mustBe Left(UnexpectedGetStatusFailure(INTERNAL_SERVER_ERROR))
    }

  }

}
