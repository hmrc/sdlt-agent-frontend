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
import controllers.routes.{JourneyRecoveryController, SystemErrorController}
import models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages.manageAgents.{AgentCheckYourAnswersPage, ConfirmAgentContactDetailsPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AddressLookupController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         val addressLookupService: AddressLookupService,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         stornRequiredAction: StornRequiredAction,
                                         navigator: Navigator
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with LoggingUtil {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
    addressLookupService.initJourney(request.userAnswers, request.storn, mode).map {
      case Right(JourneyInitSuccessResponse(Some(addressLookupLocation))) =>
        logger.debug(s"[AddressLookupController][onPageLoad] - Journey initiated: ${addressLookupLocation}")
        Redirect(addressLookupLocation)
      case Right(models.responses.addresslookup.JourneyInitResponse.JourneyInitSuccessResponse(None)) =>
        logger.error("[AddressLookupController][onPageLoad] - Failed::Location not provided")
        Redirect(JourneyRecoveryController.onPageLoad())
      case Left(ex) =>
        logger.error(s"[AddressLookupController][onPageLoad] - Failed to Init journey: $ex")
        Redirect(SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request => {
    logger.debug(s"[AddressLookupController][onSubmit] - UA: ${request.userAnswers}")
    for {
      id <- EitherT(Future.successful(Try {
        request.queryString.get("id").get(0)
      }.toEither))
      journeyOutcome <- EitherT(addressLookupService.getJourneyOutcome(id, request.userAnswers))
    } yield journeyOutcome
  }.value.map {
    case Right(updatedAnswer) if mode == NormalMode =>
      logger.info(s"[AddressLookupController][onSubmit] - address extracted and saved in normal mode")
      Redirect(navigator.nextPage(ConfirmAgentContactDetailsPage, NormalMode, updatedAnswer))
    case Right(updatedAnswer) if mode == CheckMode =>
      logger.info(s"[AddressLookupController][onSubmit] - address extracted and saved in check mode")
      Redirect(navigator.nextPage(AgentCheckYourAnswersPage, CheckMode, updatedAnswer))
    case _ =>
      logger.error("[AddressLookupController][onSubmit] - failed to extract address or invalid mode")
      Redirect(SystemErrorController.onPageLoad())
    }
  }

}