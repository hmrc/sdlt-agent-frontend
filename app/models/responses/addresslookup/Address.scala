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

package models.responses.addresslookup

import play.api.libs.json.{Json, OFormat}

case class Address(
                    lines:    Seq[String],
                    postcode: Option[String]
                  )

case class AddressModel(
                                 auditRef: String,
                                 address: Address
                               )

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}

object AddressModel {
  implicit val format: OFormat[AddressModel] = Json.format[AddressModel]
}

case class JourneyResultAddressModel(auditRef: String, address: Address)

object JourneyResultAddressModel {
  implicit val format: OFormat[JourneyResultAddressModel] = Json.format[JourneyResultAddressModel]
}