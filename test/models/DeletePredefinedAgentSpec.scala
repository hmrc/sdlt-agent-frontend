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

import models.requests.DeletePredefinedAgentRequest
import models.responses.DeletePredefinedAgentResponse
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class DeletePredefinedAgentSpec extends AnyWordSpec with Matchers {

  "DeletePredefinedAgentRequest" should {

    "serialize to JSON correctly" in {
      val request = DeletePredefinedAgentRequest(
        storn = "STN001",
        agentReferenceNumber = "ARN001"
      )

      val json = Json.toJson(request)

      (json \ "storn").as[String] mustBe "STN001"
      (json \ "agentReferenceNumber").as[String] mustBe "ARN001"
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "storn"                -> "STN001",
        "agentReferenceNumber" -> "ARN001"
      )

      val result = json.validate[DeletePredefinedAgentRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.storn mustBe "STN001"
      request.agentReferenceNumber mustBe "ARN001"
    }

    "fail to deserialize when storn is missing" in {
      val json = Json.obj(
        "agentReferenceNumber" -> "ARN001"
      )

      val result = json.validate[DeletePredefinedAgentRequest]

      result.isError mustBe true
    }

    "fail to deserialize when agentReferenceNumber is missing" in {
      val json = Json.obj(
        "storn" -> "STN001"
      )

      val result = json.validate[DeletePredefinedAgentRequest]

      result.isError mustBe true
    }
  }

  "DeletePredefinedAgentResponse" should {

    "serialize to JSON correctly when deleted is true" in {
      val response = DeletePredefinedAgentResponse(deleted = true)

      val json = Json.toJson(response)

      (json \ "deleted").as[Boolean] mustBe true
    }

    "serialize to JSON correctly when deleted is false" in {
      val response = DeletePredefinedAgentResponse(deleted = false)

      val json = Json.toJson(response)

      (json \ "deleted").as[Boolean] mustBe false
    }

    "deserialize from JSON correctly when deleted is true" in {
      val json = Json.obj("deleted" -> true)

      val result = json.validate[DeletePredefinedAgentResponse]

      result mustBe a[JsSuccess[_]]
      result.get.deleted mustBe true
    }

    "deserialize from JSON correctly when deleted is false" in {
      val json = Json.obj("deleted" -> false)

      val result = json.validate[DeletePredefinedAgentResponse]

      result mustBe a[JsSuccess[_]]
      result.get.deleted mustBe false
    }

    "fail to deserialize when deleted field is missing" in {
      val json = Json.obj()

      val result = json.validate[DeletePredefinedAgentResponse]

      result.isError mustBe true
    }
  }
}
