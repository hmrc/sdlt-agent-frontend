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

import controllers.actions.IdentifierAction
import models.Mode

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import controllers.JourneyRecoveryController
import controllers.actions.*
import controllers.routes.JourneyRecoveryController
import forms.manageAgents.AgentContactDetailsFormProvider
import models.manageAgents.AgentContactDetails

import javax.inject.Inject
import navigation.Navigator
import models.Mode
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgents.AgentContactDetailsView

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AgentContactDetailsController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               sessionRepository: SessionRepository,
                                               navigator: Navigator,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               stornRequiredAction: StornRequiredAction,
                                               formProvider: AgentContactDetailsFormProvider,
                                               stampDutyLandTaxService: StampDutyLandTaxService,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: AgentContactDetailsView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  val postAction = controllers.manageAgents.routes.AgentContactDetailsController.onSubmit

  def onPageLoad(mode: Mode, agentReferenceNumber: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>

      stampDutyLandTaxService
        .getAgentDetails(request.storn, agentReferenceNumber) map {
        case Some(agentDetails) =>
          val preparedForm = request.userAnswers.get(AgentContactDetailsPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          logger.error(s"session=${request.session.data}")
          Ok(view(preparedForm, mode, postAction(mode, agentReferenceNumber), agentDetails))
        case None               =>
          logger.error(s"[AgentContactDetailsController][onPageLoad] Failed to retrieve details for agent with storn: ${request.storn}")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      } recover {
        case ex =>
          logger.error("[AgentContactDetailsController][onPageLoad] Unexpected failure", ex)
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode, agentReferenceNumber: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>
      stampDutyLandTaxService
        .getAgentDetails(request.storn, agentReferenceNumber) flatMap {
        case Some(agentDetails) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, postAction(mode, agentReferenceNumber), agentDetails))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentContactDetailsPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AgentCheckYourAnswersPage, mode, updatedAnswers))
            )
        case None =>
          logger.error(s"[AgentContactDetailsController][onSubmit] Failed to retrieve details for agent with storn: ${request.storn}")
          logger.error(s"session=${request.session.data}")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
  }
}