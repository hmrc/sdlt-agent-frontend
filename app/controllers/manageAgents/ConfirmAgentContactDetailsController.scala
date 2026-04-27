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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, StornRequiredAction}
import forms.manageAgents.ConfirmAgentContactDetailsFormProvider
import models.manageAgents.ConfirmAgentContactDetails
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.{logError, logInfo}
import views.html.manageAgents.ConfirmAgentContactDetailsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmAgentContactDetailsController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      identify: IdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      stornRequiredAction: StornRequiredAction,
                                                      formProvider: ConfirmAgentContactDetailsFormProvider,
                                                      sessionRepository: SessionRepository,
                                                      navigator: Navigator,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      stampDutyLandTaxService: StampDutyLandTaxService,
                                                      view: ConfirmAgentContactDetailsView
                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      stampDutyLandTaxService.getAgentName match {
        case Left(error) =>
          logError(s"Couldn't find agent in user answers: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Right(agentName) =>
          val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)
          Ok(view(form, agentName))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>
      stampDutyLandTaxService.getAgentName match {
        case Right(agentName) =>
          val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName))),
            {
              case ConfirmAgentContactDetails.Option1 =>
                logInfo(s"[ConfirmAgentContactDetailsController][onSubmit] User selected `Yes` Redirect to AgentContactDetailsController onPageLoad()")
                Future.successful(Redirect(navigator.nextPage(AgentContactDetailsPage, NormalMode, request.userAnswers)))

              case ConfirmAgentContactDetails.Option2 =>
                request.userAnswers.get(AgentContactDetailsPage) match {
                  case Some(page) => stampDutyLandTaxService.removeAgentContactDetailsPageAndUpdateUserAnswers(request.userAnswers, sessionRepository).flatMap{
                    case Right(updatedUserAnswers) =>
                      logInfo(s"[ConfirmAgentContactDetailsController][onSubmit] User Selected `No` going back in the journey after previously selecting `Yes`")
                      Future.successful(Redirect(navigator.nextPage(AgentCheckYourAnswersPage, NormalMode, updatedUserAnswers)))
                    case Left(error) =>
                      logError(s"[ConfirmAgentContactDetailsController][onSubmit]: User Selected`No` but there are some issues, Redirect to JourneyRecoveryController:$error")
                      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                  }
                  case None =>
                    logInfo(s"[ConfirmAgentContactDetailsController][onSubmit] User selected `No` First time in the journey")
                    Future.successful(Redirect(navigator.nextPage(AgentCheckYourAnswersPage, NormalMode, request.userAnswers)))
                }
            }
          )
        case Left(error) =>
          logError(s"Couldn't find agent in user answers: $error")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
  
}
