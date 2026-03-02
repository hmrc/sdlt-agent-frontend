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
import models.responses.addresslookup.JourneyInitResponse.AddressLookupResponse
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import scala.concurrent.ExecutionContext.Implicits.global


class JourneyInitResponseISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with HttpClientV2Support
  with WireMockSupport
  with EitherValues {

  "Json to object conversion" should {

    "success when correct json provided: no header: status code 202:ACCEPTED" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStr =
        """
          |{"location":"someLocation"}
          |""".stripMargin

      stubFor(
        get(
          urlEqualTo("/addressLookUpInit"))
          .willReturn(aResponse()
            .withStatus(202)
            .withBody(jsonStr)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUpInit")
        .execute[AddressLookupResponse]
        .futureValue

      parsingOutCome.isRight mustBe true

      verify(
        getRequestedFor(urlEqualTo("/addressLookUpInit"))
      )

    }

    "success when correct json provided: with header: status code 202:ACCEPTED" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStr =
        """
          |{"location":"someLocation"}
          |""".stripMargin

      stubFor(
        get(
          urlEqualTo("/addressLookUpInit"))
          .willReturn(aResponse()
            .withHeader("location", "someLocation")
            .withStatus(202)
            .withBody(jsonStr)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUpInit")
        .execute[AddressLookupResponse]
        .futureValue

      parsingOutCome.isRight mustBe true

      verify(
        getRequestedFor(urlEqualTo("/addressLookUpInit"))
      )

    }

    "~: with header: status code any other" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val jsonStr =
        """
          |{"location":"someLocation"}
          |""".stripMargin

      stubFor(
        get(
          urlEqualTo("/addressLookUpInit"))
          .willReturn(aResponse()
            .withHeader("location", "someLocation")
            .withStatus(404)
            .withBody(jsonStr)
          )
      )
      val parsingOutCome = httpClientV2
        .get(url"$wireMockUrl/addressLookUpInit")
        .execute[AddressLookupResponse]
        .futureValue

      parsingOutCome.isLeft mustBe true

      verify(
        getRequestedFor(urlEqualTo("/addressLookUpInit"))
      )

    }

  }

}

