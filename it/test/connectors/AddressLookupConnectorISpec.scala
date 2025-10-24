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

import itutil.ApplicationWithWiremock
import models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

class AddressLookupConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: AddressLookupConnector = app.injector.instanceOf[AddressLookupConnector]

  private val storn = "STN001"
  val addressLookUpInit = "/stamp-duty-land-tax-agent/address-lookup/init"

  // JourneyInitFailureResponse, JourneyInitSuccessResponse
  "Initiate Address Lookup Journey:: success" should {
    // TODO: add stub ?
    val expectedLocation : String = "someDummyLocation"
    val result = connector.initJourney.futureValue
    result mustBe JourneyInitSuccessResponse(Some(expectedLocation))
  }

  "Initiate Address Lookup Journey:: failed" should {

  }

  "Extract Address Details upon Journey completion" should {

  }

}
