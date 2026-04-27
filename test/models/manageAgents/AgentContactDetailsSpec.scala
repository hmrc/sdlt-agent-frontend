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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class AgentContactDetailsSpec extends AnyWordSpec with Matchers {

  "RemoveAgent" should {

    "serialize to JSON correctly if all fields are present" in {
      val contactDetails = AgentContactDetails(
        phone = Some("0123456789"),
        email = Some("thomastkelly@gmail.com")
      )

      val json = Json.toJson(contactDetails)

      json.toString must include("0123456789")
      json.toString must include("thomastkelly@gmail.com")
    }

    "deserialize from JSON correctly" in {
      val jsonString =
        """
          |{
          |  "phone": "0123456789",
          |  "email": "thomastkelly@gmail.com"
          |}
          |""".stripMargin

      val json = Json.parse(jsonString)
      val contactDetails = json.as[AgentContactDetails]

      contactDetails.phone mustBe Some("0123456789")
      contactDetails.email mustBe Some("thomastkelly@gmail.com")
    }
  }
}
