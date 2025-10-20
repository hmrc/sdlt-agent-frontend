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

package config

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.Lang
import play.api.inject.guice.GuiceApplicationBuilder

class FrontendAppConfigSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "host" -> "http://localhost:9000",
        "appName" -> "test-app",
        "contact-frontend.host" -> "http://localhost:9250",
        "urls.login" -> "http://localhost:9949/auth-login-stub/gg-sign-in",
        "urls.loginContinue" -> "http://localhost:9000/sdlt-filing-frontend",
        "urls.signOut" -> "http://localhost:9025/gg/sign-out",
        "features.welsh-translation" -> false,
        "timeout-dialog.timeout" -> 900,
        "timeout-dialog.countdown" -> 120,
        "mongodb.timeToLiveInSeconds" -> 900,
        "microservice.services.protocol" -> "http",
        "microservice.services.test-service.protocol" -> "https",
        "microservice.services.test-service.host" -> "example.com",
        "microservice.services.test-service.port" -> 9000,
        "microservice.services.another-service.protocol" -> "https",
        "microservice.services.another-service.host" -> "different.com",
        "microservice.services.another-service.port" -> 8080,
        "microservice.services.feedback-frontend.protocol" -> "http",
        "microservice.services.feedback-frontend.host" -> "localhost",
        "microservice.services.feedback-frontend.port" -> 9514
      )
      .build()

  private lazy val appConfig = app.injector.instanceOf[FrontendAppConfig]

  "FrontendAppConfig" - {

    "getConfString" - {

      "must return config value when key exists" in {
        val result = appConfig.getConfString("test-service.host", "default-value")

        result mustBe "example.com"
      }

      "must return default value when key does not exist" in {
        val result = appConfig.getConfString("non-existent-key", "default-value")

        result mustBe "default-value"
      }
    }

    "getConfInt" - {

      "must return config value when key exists" in {
        val result = appConfig.getConfInt("test-service.port", 8080)

        result mustBe 9000
      }

      "must return default value when key does not exist" in {
        val result = appConfig.getConfInt("non-existent-key", 8080)

        result mustBe 8080
      }
    }

    "baseUrl" - {

      "must construct URL with protocol, host and port from config" in {
        val result = appConfig.baseUrl("test-service")

        result mustBe "https://example.com:9000"
      }

      "must handle different service names" in {
        val result = appConfig.baseUrl("another-service")

        result mustBe "https://different.com:8080"
      }

      "must throw RuntimeException when service config is missing" in {
        val exception = intercept[RuntimeException] {
          appConfig.baseUrl("non-existent-service")
        }

        exception.getMessage must include("Could not find config key")
      }
    }

    "languageMap" - {

      "must return map with English and Welsh languages" in {
        val result = appConfig.languageMap

        result must contain("en" -> Lang("en"))
        result must contain("cy" -> Lang("cy"))
        result.size mustBe 2
      }
    }
  }
}
