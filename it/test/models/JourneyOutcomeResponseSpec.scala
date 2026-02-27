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
import models.responses.addresslookup.JourneyOutcomeResponse.{AddressLookupJourneyOutcome,
  getAddressLookupDetailsHttpReads}
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyOutcomeResponseSpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with HttpClientV2Support
  with WireMockSupport
  with EitherValues {

  //TODO: keep on working on this next Monday morning
  // Add case for NOT_FOUND and UnexpectedGetStatusFailure case etc
  "Json to object conversion" should {

    "success when correct json provided" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

     val jsonStr =
              """
                |{"auditRef":"ref","address":{"lines":["line 1"],"postcode":"SE19 2WE"}}
                |""".stripMargin
            val js = Json.parse(jsonStr)

      stubFor(
        get(
          urlEqualTo("/addressLookUp"))
          .willReturn(aResponse()
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

    "failure when wrong json provided" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStrWrong =
          """
            |{}
            |""".stripMargin

      stubFor(
        get(
          urlEqualTo("/addressLookUp"))
          .willReturn(aResponse()
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
  }

}