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

import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object JourneyInitResponse {

  case class JourneyInitSuccessResponse(location: Option[String])

  case class JourneyInitFailureResponse(status: Int)
  
  type AddressLookupResponse = Either[JourneyInitFailureResponse, JourneyInitSuccessResponse]

  implicit def postAddressLookupHttpReads: HttpReads[AddressLookupResponse] =
    new HttpReads[AddressLookupResponse] {

      override def read(method: String, url: String, response: HttpResponse): AddressLookupResponse = {
        response.status match {
          case ACCEPTED => Right(
            // TODO: know env level bug:: case-sensitive / more clear fix???
            if (response.header(key = "location").isEmpty) {
              JourneyInitSuccessResponse(response.header(key = "Location"))
            } else {
              JourneyInitSuccessResponse(response.header(key = "location"))
            }
          )
          case status =>
            Left(JourneyInitFailureResponse(status))
        }
      }

    }
}