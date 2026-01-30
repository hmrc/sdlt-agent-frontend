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

package models.responses.organisation

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class CreatedAgent(
    storn: String,
    agentId: Option[String],
    name: String,
    houseNumber: Option[String],
    address1: String,
    address2: Option[String],
    address3: Option[String],
    address4: Option[String],
    postcode: Option[String],
    phone: Option[String],
    email: Option[String],
    dxAddress: Option[String],
    agentResourceReference: String
) {

  def getAddressLine1AndAddressLine2: String =
    address2.fold(address1)(value => s"$address1 $value")
}

object CreatedAgent {
  implicit val format: OFormat[CreatedAgent] = Json.format[CreatedAgent]
}
