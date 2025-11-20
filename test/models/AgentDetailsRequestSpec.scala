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

package models

import generators.AgentDetailsRequestGenerator
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json, JsonValidationError, __}

class AgentDetailsRequestSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with AgentDetailsRequestGenerator {

  "AgentDetailsRequest" - {
    "must serialise into json from agentDetails" in {
      forAll(nonEmptyString, nonEmptyString, nonEmptyString, nonEmptyString, nonEmptyString) {
        (agentName, addressLine1, addressLine2, addressLine3, addressLine4) => {
          val agentDetails = AgentDetailsRequest(
            agentName = agentName,
            addressLine1 = Some(addressLine1),
            addressLine2 = Some(addressLine2),
            addressLine3 = Some(addressLine3),
            addressLine4 = Some(addressLine4),
            postcode = Some("L234GF"),
            phone = Some("98765432"),
            email = Some("tyson31@gmail.com")
          )
          Json.toJson(agentDetails) mustEqual Json.parse(
            s"""
               |{
               |"agentName":"$agentName",
               |"addressLine1":"$addressLine1",
               |"addressLine2":"$addressLine2",
               |"addressLine3":"$addressLine3",
               |"addressLine4":"$addressLine4",
               |"postcode":"L234GF",
               |"phone":"98765432",
               |"email":"tyson31@gmail.com"
               |}
               |""".stripMargin)
        }
      }
    }
    "must serialise into json when only agentName is given" in {
      val nonEmptyString: Gen[String] = Gen.alphaNumStr.suchThat(_.nonEmpty)
      forAll(nonEmptyString) {
        agentName => {
          val agentDetails = AgentDetailsRequest(
            agentName = agentName,
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            postcode = None,
            phone = None,
            email = None
          )
          Json.toJson(agentDetails) mustEqual Json.parse(
            s"""
               |{
               |"agentName":"$agentName"
               |}
               |""".stripMargin)
        }
      }
    }

    "must deserialize from mongo" - {
      "when agent name is given" in {
        val nonEmptyString: Gen[String] = Gen.alphaNumStr.suchThat(_.nonEmpty)
        forAll(nonEmptyString) {
          agentName => {
            val agentDetailsRequest = AgentDetailsRequest(
              agentName = agentName,
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
                   | "address":{
                   | "lines":[
                   | "WoodLane"
                   | ],
                   | "postcode": "DY28AB"
                   | }
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
             | "address":{
             | "lines":[
             | "WoodLane"
             | ],
             | "postcode": "DY28AB"
             | }
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