/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import com.github.tomakehurst.wiremock.client.WireMock.*
import models.responses.addresslookup.JourneyOutcomeResponse.{
  AddressLookupJourneyOutcome,
  getAddressLookupDetailsHttpReads
}
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyOutcomeResponseISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with HttpClientV2Support
    with WireMockSupport
    with EitherValues {

  "Json to object conversion" should {

    "success when correct json provided: status code 200:OK" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStr =
        """
                |{"auditRef":"ref","address":{"lines":["line 1"],"postcode":"SE19 2WE"}}
                |""".stripMargin
      stubFor(
        get(urlEqualTo("/addressLookUp"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(jsonStr)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUp")
        .execute[AddressLookupJourneyOutcome]
        .futureValue

      parsingOutCome.isRight mustBe true

      verify(
        getRequestedFor(urlEqualTo("/addressLookUp"))
      )

    }

    "failure when wrong json provided: status code 200:OK" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStrWrong =
        """
            |{}
            |""".stripMargin

      stubFor(
        get(urlEqualTo("/addressLookUp"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(jsonStrWrong)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUp")
        .execute[AddressLookupJourneyOutcome]
        .futureValue

      parsingOutCome.isLeft mustBe true

      verify(
        getRequestedFor(urlEqualTo("/addressLookUp"))
      )

    }

    "when status code 404:NOT_FOUND" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStrWrong =
        """
          |{}
          |""".stripMargin

      stubFor(
        get(urlEqualTo("/addressLookUp"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withBody(jsonStrWrong)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUp")
        .execute[AddressLookupJourneyOutcome]
        .futureValue

      parsingOutCome mustBe Right(None)

      verify(
        getRequestedFor(urlEqualTo("/addressLookUp"))
      )

    }

    "when any other error status code: 500:INTERNAL_SERVER_ERROR" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStrWrong =
        """
          |{}
          |""".stripMargin

      stubFor(
        get(urlEqualTo("/addressLookUp"))
          .willReturn(
            aResponse()
              .withStatus(500)
              .withBody(jsonStrWrong)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUp")
        .execute[AddressLookupJourneyOutcome]
        .futureValue

      parsingOutCome.isLeft mustBe true

      verify(
        getRequestedFor(urlEqualTo("/addressLookUp"))
      )

    }
  }

}
