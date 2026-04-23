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
import models.{NormalMode, UserAnswers}
import models.manageAgents.ConfirmAgentContactDetails
import navigation.Navigator
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage, AgentNamePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.{logError, logInfo}
import views.html.manageAgents.ConfirmAgentContactDetailsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ConfirmAgentContactDetailsController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      identify: IdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      stornRequiredAction: StornRequiredAction,
                                                      formProvider: ConfirmAgentContactDetailsFormProvider,
                                                      sessionRepository: SessionRepository,
                                                      stampDutyLandTaxService: StampDutyLandTaxService,
                                                      navigator: Navigator,
                                                      val controllerComponents: MessagesControllerComponents,
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
                Future.successful(Redirect(navigator.nextPage(AgentContactDetailsPage, NormalMode, request.userAnswers)))

              case ConfirmAgentContactDetails.Option2 =>
                request.userAnswers.get(AgentContactDetailsPage) match {
                  case Some(page) => removePageUpdateUserAnswersAndRedirect(request.userAnswers)
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

  private def removePageUpdateUserAnswersAndRedirect(userAnswers: UserAnswers): Future[Result] = {
    userAnswers.remove(AgentContactDetailsPage).map { updatedUserAnswers =>
        logInfo(s"[ConfirmAgentContactDetailsController][onSubmit] User Selected `No` going back in the journey after previously selecting `Yes`")
        sessionRepository.set(updatedUserAnswers).map { value =>
            Redirect(navigator.nextPage(AgentCheckYourAnswersPage, NormalMode, updatedUserAnswers))
          }
          .recover { case ex =>
            logError("[ConfirmAgentContactDetailsController][onSubmit] Failed to update Session Repository with user answers")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
      .getOrElse {
        logError(s"[ConfirmAgentContactDetailsController][onSubmit] Couldn't remove AgentContactDetailsPage")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }


}
