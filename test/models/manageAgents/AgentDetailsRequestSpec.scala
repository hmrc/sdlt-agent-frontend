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

import models.AgentDetailsRequest
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsResultException, Json, JsonValidationError, __}

class AgentDetailsRequestSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  "AgentDetailsRequest" - {
    "must deserialize from mongo" - {
      "when agent name is given" in {
        val nonEmptyString: Gen[String] = Gen.alphaNumStr.suchThat(_.nonEmpty)
        forAll(nonEmptyString) {
          agentName => {
            val agentDetailsRequest = AgentDetailsRequest(
              agentName = agentName,
              houseNumber = None,
              addressLine1 = Some("WoodLane"),
              addressLine2 = None,
              addressLine3 = None,
              addressLine4 = None,
              postcode = Some("DY28AB"),
              phone = Some("123456"),
              email = Some("testexample@email.com")
            )
            Json
              .parse(
                s"""
                   |{
                   |"agentName":"$agentName",
                   |"agentAddress":{
                   |    "address":{
                   |    "lines":[
                   |    "WoodLane"
                   |    ],
                   |    "postcode": "DY28AB"
                   |      }
                   |},
                   |"agentContactDetails":{
                   |"phone":"123456",
                   |"email":"testexample@email.com"
                   |}
                   |}
                   |""".stripMargin)
              .as[AgentDetailsRequest] mustBe agentDetailsRequest

          }
        }

      }
    }
    "must fail to deserialize from mongo" - {
      "when agent name is not given" in {
        val jsonWithMissingAgentName = Json.parse(
          s"""
             |{
             |"agentAddress":{
             |    "address":{
             |    "lines":[
             |    "WoodLane"
             |    ],
             |    "postcode": "DY28AB"
             |      }
             |},
             |"agentContactDetails":{
             |"phone":"123456",
             |"email":"testexample@email.com"
             |}
             |}
             |""".stripMargin)


        val result = jsonWithMissingAgentName.validate[AgentDetailsRequest]
        result mustEqual JsError(Seq(
          __ \ "agentName" -> Seq(JsonValidationError("error.path.missing"))
        ))

      }
    }

  }
}




