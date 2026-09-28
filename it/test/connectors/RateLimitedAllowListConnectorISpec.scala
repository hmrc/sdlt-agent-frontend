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

import itutil.AllowListStub.stubCheckAllowList
import itutil.ApplicationWithWiremock
import models.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

class RateLimitedAllowListConnectorISpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ApplicationWithWiremock {

  private val connector = app.injector.instanceOf[RateLimitedAllowListConnector]
  given HeaderCarrier   = HeaderCarrier()

  "checkAllowList" should {

    "return true when backend returns 200 with included true" in {

      val response = AllowListCheckResponse(included = true)

      stubCheckAllowList("sdlt-management-frontend", "test-feature")(OK, Json.toJson(response))

      val result = connector.checkAllowList("test-feature", "1234567890").futureValue

      result mustBe true
    }

    "return false when backend returns 200 with included false" in {

      val response = AllowListCheckResponse(included = false)

      stubCheckAllowList("sdlt-management-frontend", "test-feature")(OK, Json.toJson(response))

      val result = connector.checkAllowList("test-feature", "1234567890").futureValue

      result mustBe false
    }
  }

}
