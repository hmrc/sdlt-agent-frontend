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

package models.requests

import generators.CreatePredefinedAgentRequestGenerator
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.libs.json.JsonValidationError
import play.api.libs.json.__

class CreatePredefinedAgentRequestSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with CreatePredefinedAgentRequestGenerator {

  "CreatePredefinedAgentRequest" - {
    "must serialise into json from agentDetails" in {
      val testSorn: String = "STN001"
      forAll(
        nonEmptyString,
        nonEmptyString,
        nonEmptyString,
        nonEmptyString,
        nonEmptyString
      ) { (agentName, addressLine1, addressLine2, addressLine3, addressLine4) =>
        {
          val createPredefinedAgentRequest = CreatePredefinedAgentRequest(
            storn = testSorn,
            agentName = agentName,
            addressLine1 = Some(addressLine1),
            addressLine2 = Some(addressLine2),
            addressLine3 = Some(addressLine3),
            addressLine4 = Some(addressLine4),
            postcode = Some("L234GF"),
            phone = Some("98765432"),
            email = Some("tyson31@gmail.com")
          )
          Json.toJson(createPredefinedAgentRequest) mustEqual Json.parse(s"""
               |{
               |"storn": "$testSorn",
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
    "must serialise into json when only agentName and storn is given" in {
      forAll(nonEmptyString, nonEmptyString) { (storn, agentName) =>
        {
          val createPredefinedAgentRequest = CreatePredefinedAgentRequest(
            storn = storn,
            agentName = agentName,
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            postcode = None,
            phone = None,
            email = None
          )
          Json.toJson(createPredefinedAgentRequest) mustEqual Json.parse(s"""
               |{
               |"storn": "$storn",
               |"agentName":"$agentName"
               |}
               |""".stripMargin)
        }
      }
    }

    "must deserialize from mongo" - {
      "when agent name and storn are given" in {
        forAll(nonEmptyString, nonEmptyString) { (storn, agentName) =>
          {
            val createPredefinedAgentRequest = CreatePredefinedAgentRequest(
              storn = storn,
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
              .parse(s"""
                   |{
                   |"storn": "$storn",
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
              .as[
                CreatePredefinedAgentRequest
              ] mustBe createPredefinedAgentRequest

          }
        }

      }
    }
    "must fail to deserialize from mongo" - {
      "when agentName is not given" in {
        val jsonWithMissingAgentName = Json.parse(s"""
             |{
             |"storn":"STN001",
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

        val result =
          jsonWithMissingAgentName.validate[CreatePredefinedAgentRequest]
        result mustEqual JsError(
          Seq(
            __ \ "agentName" -> Seq(JsonValidationError("error.path.missing"))
          )
        )

      }
      "when storn  is not given" in {
        val jsonWithMissingStorn = Json.parse(s"""
             |{
             |"agentName": "mark",
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

        val result = jsonWithMissingStorn.validate[CreatePredefinedAgentRequest]
        result mustEqual JsError(
          Seq(
            __ \ "storn" -> Seq(JsonValidationError("error.path.missing"))
          )
        )

      }
      "when storn and agentName  are not given" in {
        val jsonWithMissingStornAndAgentName = Json.parse(s"""
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

        val result = jsonWithMissingStornAndAgentName
          .validate[CreatePredefinedAgentRequest]
        result mustEqual JsError(
          Seq(
            __ \ "agentName" -> Seq(JsonValidationError("error.path.missing")),
            __ \ "storn" -> Seq(JsonValidationError("error.path.missing"))
          )
        )

      }
    }

  }
}
