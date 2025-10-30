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

package controllers.manageAgents

import cats.data.EitherT
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, StornRequiredAction}
import controllers.routes.JourneyRecoveryController
import models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.manageAgents.AgentContactDetailsPage
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddressLookupController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         val addressLookupService: AddressLookupService,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         stornRequiredAction: StornRequiredAction,
                                         navigator: Navigator
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
    addressLookupService.initJourney(request.userAnswers, request.storn).map {
      case Right(JourneyInitSuccessResponse(Some(addressLookupLocation))) =>
        Logger("application").debug(s"[AddressLookupController] - Journey initiated: ${addressLookupLocation}")
        Redirect(addressLookupLocation)
      case Right(models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse(None)) =>
        Logger("application").error("[AddressLookupController] - Failed::Location not provided")
        Redirect(JourneyRecoveryController.onPageLoad())
      case Left(ex) =>
        Logger("application").error(s"[AddressLookupController] - Failed to Init journey: $ex")
        Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request => {
    Logger("application").debug(s"[AddressLookupController] - UA: ${request.userAnswers}")
    for {
      id <- EitherT(Future.successful(Try {
        request.queryString.get("id").get(0)
      }.toEither))
      journeyOutcome <- EitherT(addressLookupService.getJourneyOutcome(id, request.userAnswers))
    } yield journeyOutcome
  }.value.map {
    case Right(updatedAnswer) =>
      Logger("application").info(s"[AddressLookupController] - address extracted and saved")
      Redirect(navigator.nextPage(AgentContactDetailsPage, mode, updatedAnswer))
    case Left(ex) =>
      Logger("application").error(s"[AddressLookupController] - failed to extract address: ${ex}")
      Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

}