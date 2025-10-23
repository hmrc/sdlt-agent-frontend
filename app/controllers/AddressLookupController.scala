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

package controllers

import connectors.AddressLookupConnector
import models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse
import models.responses.addresslookup.JourneyOutcomeResponse.JourneyResultFailure
import models.responses.addresslookup.JourneyResultAddressModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO: this is temporarily controller to test AddressLookupConnector
class AddressLookupController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         val addressLookupConnector: AddressLookupConnector,
                                         view: IndexView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def initJourney(): Action[AnyContent] = Action.async { implicit request =>
    addressLookupConnector.initJourney.map {
      case Right(JourneyInitSuccessResponse(Some(addressLookupLocation))) =>
        Logger("application").info(s"[AddressLookupController] - OK: ${addressLookupLocation}")
        Redirect(addressLookupLocation)
      case _ =>
        Logger("application").error(s"[AddressLookupController] - ERROR")
        Ok(view())
    }
  }

  def collectAddressDetails(id: String): Action[AnyContent] = Action.async { implicit request =>
    addressLookupConnector.getJourneyOutcome(id).map {
      case Right(Some(address)) =>
        Logger("application").info(s"[AddressLookupController] - AddressFound: ${address}")
        Ok(view())
      case Right(None) =>
        Logger("application").error(s"[AddressLookupController] - Empty result")
        Ok(view())
      case Left(failure) =>
        Logger("application").error(s"[AddressLookupController] - failed to extract address: ${id} - ${failure}")
        Ok(view())
    }
  }

}