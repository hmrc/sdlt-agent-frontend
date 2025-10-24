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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.i18n.MessagesApi
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class AddressLookupConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock
  with MockHttpV2 {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  // TODO: move this under TestHelper?
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  private val storn = "STN001"

  object TestAddressLookupConnector extends AddressLookupConnector(appConfig, mockHttpClient, messagesApi)


  "Initiate Address Lookup Journey" should {

    "return location details on success" in {
      setupMockHttpPost(TestAddressLookupConnector.addressLookupInitializeUrl)(
        Right(JourneyInitSuccessResponse(Some("Some location")))
      )
      val result = TestAddressLookupConnector.initJourney.futureValue
      result mustBe Right(JourneyInitSuccessResponse(Some("Some location")))
    }

    "return location details on failure" in {
      setupMockHttpPost(TestAddressLookupConnector.addressLookupInitializeUrl)(
        Left( JourneyInitFailureResponse(INTERNAL_SERVER_ERROR) )
      )
      val result = TestAddressLookupConnector.initJourney.futureValue
      result mustBe Left( JourneyInitFailureResponse(INTERNAL_SERVER_ERROR) )
    }
  }

// TODO: continue with this part of the test on Monday
//  "Extract Address Details upon Journey completion" should {
//
//  }

}
