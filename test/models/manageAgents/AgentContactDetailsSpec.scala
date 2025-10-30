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

package models.manageAgents

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, JsValue, Json}

class AgentContactDetailsSpec extends AnyFreeSpec with Matchers {

  "AgentContactDetails" - {

    "must serialise to JSON correctly" in {

      val model = AgentContactDetails("0123456789", "agent@example.com")

      val json: JsValue = Json.toJson(model)

      json mustBe Json.obj(
        "contactTelephoneNumber" -> "0123456789",
        "contactEmail" -> "agent@example.com"
      )
    }

    "must deserialise from JSON correctly" in {

      val json = Json.obj(
        "contactTelephoneNumber" -> "0123456789",
        "contactEmail" -> "agent@example.com"
      )

      json.validate[AgentContactDetails] mustBe JsSuccess(
        AgentContactDetails("0123456789", "agent@example.com")
      )
    }

    "must round-trip (serialise then deserialise back to same model)" in {

      val original = AgentContactDetails("02079460000", "info@test.co.uk")
      val json = Json.toJson(original)
      val parsed = json.as[AgentContactDetails]

      parsed mustEqual original
    }
  }
}