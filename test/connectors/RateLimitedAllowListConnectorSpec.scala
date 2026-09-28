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

package connectors

import base.SpecBase
import com.typesafe.config.ConfigFactory
import models.{AllowListCheckRequest, AllowListCheckResponse}
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RateLimitedAllowListConnectorSpec extends SpecBase with MockitoSugar with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val actorSystem: ActorSystem = ActorSystem("unit-tests")

  override protected def afterAll(): Unit =
    actorSystem.terminate()

  val config: Configuration = Configuration(
    ConfigFactory.parseString(
      """
        |microservice {
        |   services {
        |     rate-limited-allow-list  {
        |       protocol = http
        |       host     = foo.bar.com
        |       port     = 1234
        |     }
        |   }
        |}
        |splitter {
        |   serviceName = sdlt-management-frontend
        |   allowListName = beta
        |}
        |http-verbs.retries.intervals = [10ms,50ms]
        |""".stripMargin
    )
  )

  given HeaderCarrier = HeaderCarrier()

  private val checkAllowListUrl =
    "http://foo.bar.com:1234/rate-limited-allow-list/services/sdlt-management-frontend/features/test-feature"

  private val payload: JsValue = Json.toJson(AllowListCheckRequest(identifier = "1234567890"))

  private val mockHttp           = mock[HttpClientV2]
  private val mockRequestBuilder = mock[RequestBuilder]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp, mockRequestBuilder)
  }

  private val connector =
    new RateLimitedAllowListConnectorImpl(
      http = mockHttp,
      config = config,
      servicesConfig = new ServicesConfig(config),
      actorSystem = actorSystem
    )

  private def givenPostReturns(response: HttpResponse, responses: HttpResponse*): Unit = {
    when(mockHttp.post(eqTo(URL(checkAllowListUrl)))(using any[HeaderCarrier]))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(eqTo(payload))(using any(), any(), any()))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute[HttpResponse](using any(), any()))
      .thenReturn(Future.successful(response), responses.map(Future.successful)*)
  }

  "RateLimitedAllowListConnector" - {

    "checkAllowList" - {

      "should return true when included is true" in {
        val json = Json.stringify(Json.toJson(AllowListCheckResponse(included = true)))

        givenPostReturns(HttpResponse(200, json))

        await(connector.checkAllowList("test-feature", "1234567890")) mustBe true
      }

      "should return false when included is false" in {
        val json = Json.stringify(Json.toJson(AllowListCheckResponse(included = false)))

        givenPostReturns(HttpResponse(200, json))

        await(connector.checkAllowList("test-feature", "1234567890")) mustBe false
      }

      "should throw exception when 500 three times" in {
        givenPostReturns(HttpResponse(500, ""), HttpResponse(500, ""), HttpResponse(500, ""))

        an[Exception] must be thrownBy {
          await(connector.checkAllowList("test-feature", "1234567890"))
        }
        verify(mockRequestBuilder, times(3)).execute[HttpResponse](using any(), any())
      }

      "should retry and succeed on a second attempt" in {
        val json = Json.stringify(Json.toJson(AllowListCheckResponse(included = true)))

        givenPostReturns(HttpResponse(500, ""), HttpResponse(200, json))

        await(connector.checkAllowList("test-feature", "1234567890")) mustBe true
      }

      "should retry and succeed on a third attempt" in {
        val json = Json.stringify(Json.toJson(AllowListCheckResponse(included = true)))

        givenPostReturns(HttpResponse(500, ""), HttpResponse(500, ""), HttpResponse(200, json))

        await(connector.checkAllowList("test-feature", "1234567890")) mustBe true
      }

    }

  }
}
