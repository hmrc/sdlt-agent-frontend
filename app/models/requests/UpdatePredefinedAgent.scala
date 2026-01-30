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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

case class UpdatePredefinedAgent(
    agentResourceReference: Option[String],
    storn: String,
    agentName: String,
    houseNumber: Option[String],
    addressLine1: Option[String],
    addressLine2: Option[String],
    addressLine3: Option[String],
    addressLine4: Option[String],
    postcode: Option[String],
    phone: Option[String],
    email: Option[String],
    dxAddress: Option[String]
)

object UpdatePredefinedAgent {

  implicit val reads: Reads[UpdatePredefinedAgent] = (
    (__ \ "agentResourceReference").readNullable[String] and
      (__ \ "storn").read[String] and
      (__ \ "agentName").read[String] and
      (__ \ "agentAddress" \ "address" \ "houseNumber").readNullable[String] and
      (__ \ "agentAddress" \ "address" \ "lines")
        .read[Seq[String]]
        .map(_.headOption) and
      (__ \ "agentAddress" \ "address" \ "lines")
        .read[Seq[String]]
        .map(_.lift(1)) and
      (__ \ "agentAddress" \ "address" \ "lines")
        .read[Seq[String]]
        .map(_.lift(2)) and
      (__ \ "agentAddress" \ "address" \ "lines")
        .read[Seq[String]]
        .map(_.lift(3)) and
      (__ \ "agentAddress" \ "address" \ "postcode").readNullable[String] and
      (__ \ "agentContactDetails" \ "phone").readNullable[String] and
      (__ \ "agentContactDetails" \ "email").readNullable[String] and
      (__ \ "dxAddress").readNullable[String]
  )(UpdatePredefinedAgent.apply _)

  implicit val format: OWrites[UpdatePredefinedAgent] =
    Json.writes[UpdatePredefinedAgent]
}
