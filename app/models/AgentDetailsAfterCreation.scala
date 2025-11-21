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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OFormat, Reads, __}

case class AgentDetailsAfterCreation (
                                       agentDetailsReferenceNumber : String,
                                       storn                       : String,
                                       agentName                   : String,
                                       houseNumber                 : Option[String],
                                       addressLine1                : Option[String],
                                       addressLine2                : Option[String],
                                       addressLine3                : Option[String],
                                       addressLine4                : Option[String],
                                       postcode                    : Option[String],
                                       phone                       : Option[String],
                                       email                       : Option[String]
                                     )

object AgentDetailsAfterCreation {


  implicit val reads: Reads[AgentDetailsAfterCreation] = (
    (__ \ "agentDetailsReferenceNumber").read[String] and
    (__ \ "storn").read[String] and
    (__ \ "agentName").read[String] and
    (__ \ "agentAddress" \ "address" \ "houseNumber").readNullable[String] and
    (__ \ "agentAddress" \ "address" \ "lines").read[Seq[String]].map(_.headOption) and
    (__ \ "agentAddress" \ "address" \ "lines").read[Seq[String]].map(_.lift(1)) and
    (__ \ "agentAddress" \ "address" \ "lines").read[Seq[String]].map(_.lift(2)) and
    (__ \ "agentAddress" \ "address" \ "lines").read[Seq[String]].map(_.lift(3)) and
    (__ \ "agentAddress" \ "address" \ "postcode").readNullable[String] and
    (__ \ "agentContactDetails" \ "phone").readNullable[String] and
    (__ \ "agentContactDetails" \ "email").readNullable[String]
    )(AgentDetailsAfterCreation.apply _)


  implicit val format: OFormat[AgentDetailsAfterCreation] = Json.format[AgentDetailsAfterCreation]
}


