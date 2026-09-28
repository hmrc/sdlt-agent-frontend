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

package itutil

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}

object AllowListStub:

  def stubCheckAllowList(service: String, feature: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    stubFor(
      post(urlPathEqualTo(s"/rate-limited-allow-list/services/$service/features/$feature"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body.toString)
        )
    )
