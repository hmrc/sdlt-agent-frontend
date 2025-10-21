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

package itutil

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait ApplicationWithWiremock
  extends AnyWordSpec
    with GuiceOneServerPerSuite
    with BeforeAndAfterAll
    with BeforeAndAfterEach:

  lazy val wireMock = new WireMock

  val extraConfig: Map[String, Any] = {
    Map[String, Any](
      "microservice.services.auth.host"                -> WireMockConstants.stubHost,
      "microservice.services.auth.port"                -> WireMockConstants.stubPort,
      "microservice.services.stamp-duty-land-tax.host" -> WireMockConstants.stubHost,
      "microservice.services.stamp-duty-land-tax.port" -> WireMockConstants.stubPort
    )
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(extraConfig)
    .build()

  override protected def beforeAll(): Unit =
    wireMock.start()
    super.beforeAll()

  override def beforeEach(): Unit =
    wireMock.resetAll()
    super.beforeEach()

  override def afterAll(): Unit =
    wireMock.stop()
    super.afterAll()

  val baseUrl: String = s"http://localhost:$port/stamp-duty-land-tax"