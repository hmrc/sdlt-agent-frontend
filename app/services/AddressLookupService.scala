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

package services

import cats.data.EitherT
import com.google.inject.Singleton
import connectors.AddressLookupConnector
import jakarta.inject.Inject
import models.UserAnswers
import models.responses.addresslookup.JourneyInitResponse.{AddressLookupResponse, JourneyInitSuccessResponse}
import models.responses.addresslookup.JourneyResultAddressModel
import pages.manageAgents.AgentAddressDetails
import uk.gov.hmrc.http.HeaderCarrier
import repositories.SessionRepository
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupService @Inject()(
                                      addressLookUpConnector: AddressLookupConnector,
                                      sessionRepository: SessionRepository,
                                    )(implicit ec: ExecutionContext){

  // Step 1: Init AL journey
  // TODO: pass storn ID
  def initJourney(sorn: String)
                 (implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
    Logger("application").debug(s"[AddressLookupService] - Init AddressLookUp journey")
    addressLookUpConnector.initJourney(sorn)
  }

  // Step 2: extract and save AddressDetails
  private def saveAddressDetails(userId: String, addressDetailsMaybe: Option[JourneyResultAddressModel]): Future[Either[Throwable, UserAnswers]] = {
    addressDetailsMaybe match {
      case Some(addressDetails) =>
          val userAnswers = UserAnswers(id = userId)
          userAnswers.set(AgentAddressDetails, addressDetails).toEither match {
            case Right(updatedAnswers) =>
              Logger("application").debug(s"[AddressLookupService] - Update user session")
              sessionRepository.set(userAnswers)
                .map(res => Right( updatedAnswers )) // assume will always succeed
            case Left(ex) =>
              Future.successful(Left(Error("Failed to update user session")))
          }
      case None =>
        Future.successful(Left(Error("No addressDetails found")))
    }
  }

  def getJourneyOutcome(id: String, userId: String)
                       (implicit hc: HeaderCarrier): Future[Either[Throwable, UserAnswers]] = {
    {
      for {
        addressDetails <- EitherT(addressLookUpConnector.getJourneyOutcome(id))
        res <- EitherT(saveAddressDetails(userId, addressDetails))
      } yield res
    }.value
  }

}
