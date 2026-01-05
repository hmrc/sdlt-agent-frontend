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
import connectors.AddressLookupConnector
import models.responses.addresslookup.JourneyInitResponse.AddressLookupResponse
import models.responses.addresslookup.JourneyResultAddressModel
import models.{Mode, UserAnswers}
import pages.manageAgents.{AgentAddressPage, AgentNamePage}
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggerUtil.{logDebug, logInfo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupService @Inject()(
                                      addressLookUpConnector: AddressLookupConnector,
                                      sessionRepository: SessionRepository,
                                    )(implicit ec: ExecutionContext) {

  // Step 1: Init AL journey
  def initJourney(userAnswers: UserAnswers, storn: String, mode: Mode)
                 (implicit hc: HeaderCarrier, messages: Messages, rh: RequestHeader): Future[AddressLookupResponse] = {
    logInfo(s"[AddressLookupService][initJourney]")
    for {
      agentName <- Future.successful(userAnswers.get(AgentNamePage))
      initJourneyRes <- addressLookUpConnector.initJourney(agentName, mode) // set agentName as empty if nothing found
    } yield initJourneyRes
  }

  // Step 2: extract and save AddressDetails
  private def saveAddressDetails(userAnswers: UserAnswers, addressDetailsMaybe: Option[JourneyResultAddressModel]): Future[Either[Throwable, UserAnswers]] = {
    addressDetailsMaybe match {
      case Some(addressDetails) =>
        userAnswers.set(AgentAddressPage, addressDetails).toEither match {
          case Right(updatedAnswers) =>
            sessionRepository.set(updatedAnswers)
              .map(res =>
                logDebug(s"[AddressLookupService] - UpdateStatus: $res")
                Right(updatedAnswers)) // assume will always succeed
          case Left(ex) =>
            Future.successful(Left(Error("Failed to update user session")))
        }
      case None =>
        Future.successful(Left(Error("No addressDetails found")))
    }
  }

  def getJourneyOutcome(id: String, userAnswers: UserAnswers)
                       (implicit hc: HeaderCarrier): Future[Either[Throwable, UserAnswers]] = {
    {
      logInfo(s"[AddressLookupService][getJourneyOutcome]")
      for {
        addressDetails <- EitherT(addressLookUpConnector.getJourneyOutcome(id))
        res <- EitherT({
          logInfo(s"[AddressLookupService][getJourneyOutcome] - addressDetails: ${addressDetails}")
          saveAddressDetails(userAnswers, addressDetails)
        })
      } yield res
    }.value
  }

}
