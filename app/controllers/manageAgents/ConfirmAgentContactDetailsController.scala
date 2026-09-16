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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggingUtil
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
                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with LoggingUtil {
  

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      stampDutyLandTaxService.getAgentName match {
        case Left(error) =>
          logger.error("[ConfirmAgentContactDetailsController][onPageLoad] Couldn't find agent in user answers", error)
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Right(agentName) =>
          val form: Form[Boolean] = formProvider(agentName)
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
          val form: Form[Boolean] = formProvider(agentName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName, mode))),
            value =>
              for {
                updatedUserAnswers <- Future.fromTry(request.userAnswers.set(ConfirmAgentContactDetailsPage, value))
                latestUserAnswers <- Future.fromTry {
                  if !value then
                    logger.info(s"[ConfirmAgentContactDetailsController][onSubmit] Removing previously filled contact details: User Selected `No` after previously selecting `Yes`")
                    updatedUserAnswers.remove(AgentContactDetailsPage)
                  else Success(updatedUserAnswers)
                }
                _ <- sessionRepository.set(latestUserAnswers)
              } yield {
                if(value){
                  logger.info(s"[ConfirmAgentContactDetailsController][onSubmit] User selected `Yes`: Redirect to AgentContactDetailsController")
                  Redirect(navigator.nextPage(AgentContactDetailsPage, mode, latestUserAnswers))
                }
                else {
                  logger.info(s"[ConfirmAgentContactDetailsController][onSubmit] User selected `No` first time in the journey")
                  Redirect(navigator.nextPage(AgentCheckYourAnswersPage, mode, latestUserAnswers))
                }
              }
          )
        case Left(error) =>
          logger.error("[ConfirmAgentContactDetailsController][onSubmit] Couldn't find agent in user answers", error)
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
  
}
