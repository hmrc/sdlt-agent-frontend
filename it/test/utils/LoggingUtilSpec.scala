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

package utils

import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.slf4j.{Logger, MDC, MarkerFactory}
import play.api.MarkerContext
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HeaderNames, HttpResponse, SessionKeys}

class LoggingUtilSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  given Materializer = app.materializer

  class TestLoggingUtil extends LoggingUtil

  val testLogger = new TestLoggingUtil()

  given MarkerContext = MarkerContext(MarkerFactory.getMarker("TEST"))

  "trueClientIp" should {
    "extract trueClientIp from request headers when present" in {
      val request = FakeRequest().withHeaders(HeaderNames.trueClientIp -> "192.168.1.1")

      testLogger.trueClientIp(request) shouldBe Some("trueClientIp: 192.168.1.1 ")
    }

    "return None when trueClientIp header is not present" in {
      val request = FakeRequest()

      testLogger.trueClientIp(request) shouldBe None
    }
  }

  "sessionId" should {
    "extract sessionId from request session when present" in {
      val request = FakeRequest().withSession(SessionKeys.sessionId -> "session-123")

      testLogger.sessionId(request) shouldBe Some("sessionId: session-123 ")
    }

    "return None when sessionId is not present in session" in {
      val request = FakeRequest()

      testLogger.sessionId(request) shouldBe None
    }
  }

  "identifiers" should {
    "combine trueClientIp and sessionId when both are present" in {
      val request = FakeRequest()
        .withHeaders(HeaderNames.trueClientIp -> "192.168.1.1")
        .withSession(SessionKeys.sessionId -> "session-123")

      testLogger.identifiers(request) shouldBe "trueClientIp: 192.168.1.1 sessionId: session-123 "
    }

    "return only trueClientIp when sessionId is not present" in {
      val request = FakeRequest()
        .withHeaders(HeaderNames.trueClientIp -> "192.168.1.1")

      testLogger.identifiers(request) shouldBe "trueClientIp: 192.168.1.1 "
    }

    "return only sessionId when trueClientIp is not present" in {
      val request = FakeRequest()
        .withSession(SessionKeys.sessionId -> "session-123")

      testLogger.identifiers(request) shouldBe "sessionId: session-123 "
    }

    "return empty string when neither identifier is present" in {
      val request = FakeRequest()

      testLogger.identifiers(request) shouldBe ""
    }
  }

  "trueClientIpFromHttpResponse" should {
    "extract trueClientIp from HttpResponse headers when present" in {
      val httpResponse = HttpResponse(
        status = 200,
        body = "",
        headers = Map(HeaderNames.trueClientIp -> Seq("192.168.1.1"))
      )

      testLogger.trueClientIpFromHttpResponse(httpResponse) shouldBe Some("trueClientIp: List(192.168.1.1)")
    }

    "return None when trueClientIp header is not present" in {
      val httpResponse = HttpResponse(status = 200, body = "")

      testLogger.trueClientIpFromHttpResponse(httpResponse) shouldBe None
    }
  }

  "sessionIdFromHttpResponse" should {
    "extract sessionId from HttpResponse headers when present" in {
      val httpResponse = HttpResponse(
        status = 200,
        body = "",
        headers = Map(HeaderNames.xSessionId -> Seq("session-123"))
      )

      testLogger.sessionIdFromHttpResponse(httpResponse) shouldBe Some("sessionId: List(session-123)")
    }

    "return None when sessionId header is not present" in {
      val httpResponse = HttpResponse(status = 200, body = "")

      testLogger.sessionIdFromHttpResponse(httpResponse) shouldBe None
    }
  }

  "identifiersFromHttpResponse" should {
    "combine trueClientIp and sessionId when both are present" in {
      val httpResponse = HttpResponse(
        status = 200,
        body = "",
        headers = Map(
          HeaderNames.trueClientIp -> Seq("192.168.1.1"),
          HeaderNames.xSessionId -> Seq("session-123")
        )
      )

      testLogger.identifiersFromHttpResponse(httpResponse) shouldBe "trueClientIp: List(192.168.1.1)sessionId: List(session-123)"
    }

    "return only trueClientIp when sessionId is not present" in {
      val httpResponse = HttpResponse(
        status = 200,
        body = "",
        headers = Map(HeaderNames.trueClientIp -> Seq("192.168.1.1"))
      )

      testLogger.identifiersFromHttpResponse(httpResponse) shouldBe "trueClientIp: List(192.168.1.1)"
    }

    "return only sessionId when trueClientIp is not present" in {
      val httpResponse = HttpResponse(
        status = 200,
        body = "",
        headers = Map(HeaderNames.xSessionId -> Seq("session-123"))
      )

      testLogger.identifiersFromHttpResponse(httpResponse) shouldBe "sessionId: List(session-123)"
    }

    "return empty string when neither identifier is present" in {
      val httpResponse = HttpResponse(status = 200, body = "")

      testLogger.identifiersFromHttpResponse(httpResponse) shouldBe ""
    }
  }

  "infoLog" should {
    "log message with identifiers from request" in {
      val request = FakeRequest()
        .withHeaders(HeaderNames.trueClientIp -> "192.168.1.1")
        .withSession(SessionKeys.sessionId -> "session-123")

      given play.api.mvc.Request[?] = request

      // This will log: "Test message (trueClientIp: 192.168.1.1 sessionId: session-123 )"
      noException should be thrownBy testLogger.infoLog("Test message")
    }
  }

  "infoConnectorLog" should {
    "log message with identifiers from HttpResponse" in {
      val httpResponse = HttpResponse(
        status = 200,
        body = "",
        headers = Map(
          HeaderNames.trueClientIp -> Seq("192.168.1.1"),
          HeaderNames.xSessionId -> Seq("session-123")
        )
      )

      given HttpResponse = httpResponse

      // This will log: "Connector message (trueClientIp: 192.168.1.1sessionId: session-123)"
      noException should be thrownBy testLogger.infoConnectorLog("Connector message")
    }
  }

  "warnLog" should {
    "log warning message with identifiers from request" in {
      val request = FakeRequest()
        .withHeaders(HeaderNames.trueClientIp -> "192.168.1.1")

      given play.api.mvc.Request[?] = request

      noException should be thrownBy testLogger.warnLog("Warning message")
    }

    "log warning message with throwable and identifiers from request" in {
      val request = FakeRequest()
        .withSession(SessionKeys.sessionId -> "session-123")
      val exception = new RuntimeException("Test exception")

      given play.api.mvc.Request[?] = request

      noException should be thrownBy testLogger.warnLog("Warning with exception", exception)
    }
  }

  "warnConnectorLog" should {
    "log warning message with identifiers from HttpResponse" in {
      val httpResponse = HttpResponse(
        status = 400,
        body = "",
        headers = Map(HeaderNames.trueClientIp -> Seq("192.168.1.1"))
      )

      given HttpResponse = httpResponse

      noException should be thrownBy testLogger.warnConnectorLog("Connector warning")
    }
  }

  "errorLog" should {
    "log error message with identifiers from request" in {
      val request = FakeRequest()
        .withHeaders(HeaderNames.trueClientIp -> "192.168.1.1")

      given play.api.mvc.Request[?] = request

      noException should be thrownBy testLogger.errorLog("Error message")
    }

    "log error message with throwable and identifiers from request" in {
      val request = FakeRequest()
        .withSession(SessionKeys.sessionId -> "session-123")
      val exception = new RuntimeException("Test exception")

      given play.api.mvc.Request[?] = request

      noException should be thrownBy testLogger.errorLog("Error with exception", exception)
    }
  }

  "errorConnectorLog" should {
    "log error message with identifiers from HttpResponse" in {
      val httpResponse = HttpResponse(
        status = 500,
        body = "",
        headers = Map(HeaderNames.xSessionId -> Seq("session-123"))
      )

      given HttpResponse = httpResponse

      noException should be thrownBy testLogger.errorConnectorLog("Connector error")
    }
  }

  "LinkLogger" should {
    "be an instance of LoggingUtil" in {
      LinkLogger shouldBe a[LoggingUtil]
    }

    "have a logger instance" in {
      LinkLogger.logger should not be null
    }
  }
}
