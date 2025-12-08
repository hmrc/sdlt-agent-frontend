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
import forms.manageAgents.RemoveAgentFormProvider
import models.manageAgents.RemoveAgent
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgents.RemoveAgentView
import controllers.routes.*

import javax.inject.{Inject, Singleton}
import models.NormalMode
import models.requests.DeletePredefinedAgentRequest
import navigation.Navigator
import pages.manageAgents.AgentOverviewPage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveAgentController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       stornRequiredAction: StornRequiredAction,
                                       formProvider: RemoveAgentFormProvider,
                                       stampDutyLandTaxService: StampDutyLandTaxService,
                                       navigator: Navigator,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RemoveAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  // TODO: Tidy up logic on this page

  val postAction: String => Call = controllers.manageAgents.routes.RemoveAgentController.onSubmit

  def onPageLoad(agentReferenceNumber: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>

      stampDutyLandTaxService
        .getAgentDetails(request.storn, agentReferenceNumber) map {
          case Some(agentDetails) => {
            val form: Form[RemoveAgent] = formProvider(agentDetails)
            Ok(view(form, postAction(agentReferenceNumber), agentDetails))
          }
          case None               =>
            logger.error(s"[RemoveAgentController][onPageLoad] Failed to retrieve details for agent with storn: ${request.storn}")
            Redirect(JourneyRecoveryController.onPageLoad())
      } recover {
        case ex =>
          logger.error("[RemoveAgentController][onPageLoad] Unexpected failure", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }
  }

  def onSubmit(agentReferenceNumber: String): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>
      stampDutyLandTaxService.getAgentDetails(request.storn, agentReferenceNumber) flatMap {
        case Some(agentDetails) =>
          val form: Form[RemoveAgent] = formProvider(agentDetails)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, postAction(agentReferenceNumber), agentDetails))),

            removeChoice =>
              removeChoice match {
                case RemoveAgent.Option1 =>
                  val req = DeletePredefinedAgentRequest(request.storn, agentReferenceNumber)
                  stampDutyLandTaxService
                    .deletePredefinedAgent(req).map { response =>
                      if(response.deleted) {
                        logger.info(s"[RemoveAgentController][onSubmit] Successfull removal of agent: storn=${request.storn} agentReferenceNumber=$agentReferenceNumber")
                        Redirect(navigator.nextPage(AgentOverviewPage, NormalMode, request.userAnswers))
                          .flashing("agentRemoved" -> agentDetails.name)
                      } else {
                        logger.error(s"[RemoveAgentController][onSubmit] Failed to remove agent: storn=${request.storn} agentReferenceNumber=$agentReferenceNumber")
                        Redirect(controllers.routes.SystemErrorController.onPageLoad())
                      }
                    }

                case RemoveAgent.Option2 =>
                  logger.info(s"[RemoveAgentController][onSubmit] User cancelled removal: storn=${request.storn} agentReferenceNumber=$agentReferenceNumber")
                  Future.successful(Redirect(navigator.nextPage(AgentOverviewPage, NormalMode, request.userAnswers)))
              }
          )

        case None =>
          logger.error(s"[RemoveAgentController][onSubmit] Agent not found for storn=${request.storn} agentReferenceNumber=$agentReferenceNumber")
          Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
      } recover {
        case ex =>
          logger.error("[RemoveAgentController][onSubmit] Unexpected failure", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }
  }
}
