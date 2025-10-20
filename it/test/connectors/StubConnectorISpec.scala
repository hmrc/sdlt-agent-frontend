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

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.AgentDetails
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.*
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class StubConnectorISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with WireMockHelper {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.stamp-duty-land-tax-stub.host" -> "localhost",
        "microservice.services.stamp-duty-land-tax-stub.port" -> server.port(),
        "microservice.services.stamp-duty-land-tax-stub.protocol" -> "http"
      )
      .build()

  private lazy val connector = app.injector.instanceOf[StubConnector]

  private val testReturnId = "123456"

  // Complete JSON with all required fields
  private val prelimReturnJson: JsValue = Json.obj(
    "stornId" -> testReturnId,
    "name" -> "John Doe",
    "addressLine1" -> "Test Street",
    "addressLine2" -> JsNull,
    "townOrCity" -> "London",
    "county" -> "London",
    "telephoneNumber" -> "07843285599",
    "emailAddress" -> "test@example.com"
  )

  "StubConnector Integration Tests" - {

    "stubManageAgentQuestions" - {

      "must return PrelimReturn when the stub returns 200 OK" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prelimReturnJson.toString())
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).futureValue

        result mustBe a[AgentDetails]

        server.verify(
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
        )
      }

      "must handle different returnId values" in {
        val differentReturnIds = List("ABC-123", "TEST-789", "12345")

        differentReturnIds.foreach { returnId =>
          val customJson = Json.obj(
            "stornId" -> returnId,
            "name" -> "John Doe",
            "addressLine1" -> "Test Street",
            "addressLine2" -> JsNull,
            "townOrCity" -> "London",
            "county" -> "London",
            "telephoneNumber" -> "07843285599",
            "emailAddress" -> "test@example.com"
          )

          server.stubFor(
            get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
              .withQueryParam("returnId", equalTo(returnId))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(customJson.toString())
              )
          )

          val result = connector.stubManageAgentQuestions(returnId).futureValue

          result mustBe a[AgentDetails]

          server.verify(
            getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
              .withQueryParam("returnId", equalTo(returnId))
          )
        }
      }

      "must throw BadRequestException when stub returns 400 Bad Request" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withBody("Bad Request")
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400

        server.verify(
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
        )
      }

      "must throw NotFoundException when stub returns 404 Not Found" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo("NONEXISTENT"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody("Not Found")
            )
        )

        val result = connector.stubManageAgentQuestions("NONEXISTENT").failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404

        server.verify(
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500

        server.verify(
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
        )
      }

      "must throw UpstreamErrorResponse when stub returns 502 Bad Gateway" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(502)
                .withBody("Bad Gateway")
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 502
      }

      "must throw UpstreamErrorResponse when stub returns 503 Service Unavailable" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(503)
                .withBody("Service Unavailable")
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).failed.futureValue

        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 503
      }

      "must include correct headers in the request" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prelimReturnJson.toString())
            )
        )

        connector.stubManageAgentQuestions(testReturnId).futureValue

        server.verify(
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withHeader("User-Agent", equalTo("sdlt-agent-frontend"))
        )
      }

      "must handle connection errors when service is unavailable" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).failed.futureValue

        result mustBe a[Throwable]
      }

      "must correctly parse JSON response into a AgentDetails model" in {
        val detailedPrelimReturnJson = Json.obj(
          "stornId" -> testReturnId,
          "name" -> "John Doe",
          "addressLine1" -> "Test Street",
          "addressLine2" -> JsNull,
          "townOrCity" -> "London",
          "county" -> "London",
          "telephoneNumber" -> "07843285599",
          "emailAddress" -> "test@example.com"
        )

        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo("TEST-123"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(detailedPrelimReturnJson.toString())
            )
        )

        val result = connector.stubManageAgentQuestions("TEST-123").futureValue

        result mustBe a[AgentDetails]
      }

      "must handle malformed JSON response" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{invalid json}")
            )
        )

        val result = connector.stubManageAgentQuestions(testReturnId).failed.futureValue

        result mustBe a[JsonParseException]
      }

      "must make GET request to correct endpoint" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prelimReturnJson.toString())
            )
        )

        connector.stubManageAgentQuestions(testReturnId).futureValue

        server.verify(
          1,
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
        )
      }

      "must not make multiple requests for a single call" in {
        server.stubFor(
          get(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
            .withQueryParam("returnId", equalTo(testReturnId))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prelimReturnJson.toString())
            )
        )

        connector.stubManageAgentQuestions(testReturnId).futureValue

        server.verify(
          1,
          getRequestedFor(urlPathEqualTo("/stamp-duty-land-tax-stub/manage-agent/details"))
        )
      }
    }
  }
}
