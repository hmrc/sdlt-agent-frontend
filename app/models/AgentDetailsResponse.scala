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

import play.api.libs.json.{Json, OFormat}

case class AgentDetailsResponse(
                                agentName            : String,
                                houseNumber          : String,          // TODO: this field should be removed - it's meant to be part of addressLine1
                                addressLine1         : String,          // TODO: Confirm which address fields are optional
                                addressLine2         : Option[String],
                                addressLine3         : String,
                                addressLine4         : Option[String],
                                postcode             : Option[String],
                                phone                : Option[String],
                                email                : String,          // TODO: Confirmed this email field should be optional
                                agentReferenceNumber : String
) {

  def getFirstLineOfAddress: String =
    s"$houseNumber $addressLine1"
}

object AgentDetailsResponse {
  implicit val format: OFormat[AgentDetailsResponse] = Json.format[AgentDetailsResponse]
}
