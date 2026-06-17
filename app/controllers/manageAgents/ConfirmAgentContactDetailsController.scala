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
import models.Mode
import navigation.Navigator
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage, ConfirmAgentContactDetailsPage}
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
import scala.util.Success

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
  
  val form: Form[Boolean] = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      stampDutyLandTaxService.getAgentName match {
        case Left(error) =>
          logError(s"Couldn't find agent in user answers: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Right(agentName) =>
          val preparedForm = request.userAnswers.get(ConfirmAgentContactDetailsPage) match  {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, agentName, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>
      stampDutyLandTaxService.getAgentName match {
        case Right(agentName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName, mode))),
            value =>
              for {
                updatedUserAnswers <- Future.fromTry(request.userAnswers.set(ConfirmAgentContactDetailsPage, value))
                latestUserAnswers <- Future.fromTry {
                  if !value then
                    logInfo(s"[ConfirmAgentContactDetailsController][onSubmit][Removing previously filled contact details] User Selected `No` going back in the journey after previously selecting `Yes`")
                    updatedUserAnswers.remove(AgentContactDetailsPage)
                  else Success(updatedUserAnswers)
                }
                _ <- sessionRepository.set(latestUserAnswers)
              } yield {
                if(value){
                  logInfo(s"[ConfirmAgentContactDetailsController][onSubmit] User selected `Yes` Redirect to AgentContactDetailsController onPageLoad()")
                  Redirect(navigator.nextPage(AgentContactDetailsPage, mode, latestUserAnswers))
                }
                else {
                  logInfo(s"[ConfirmAgentContactDetailsController][onSubmit] User selected `No` First time in the journey")
                  Redirect(navigator.nextPage(AgentCheckYourAnswersPage, mode, latestUserAnswers))
                }
              }
          )
        case Left(error) =>
          logError(s"Couldn't find agent in user answers: $error")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
  
}
