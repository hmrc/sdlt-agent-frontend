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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.manageAgents.RemoveAgentFormProvider
import models.manageAgents.RemoveAgent
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgents.RemoveAgentView
import controllers.routes._

import scala.concurrent.{ExecutionContext, Future}

class RemoveAgentController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: RemoveAgentFormProvider,
                                       stampDutyLandTaxService: StampDutyLandTaxService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RemoveAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val form: Form[RemoveAgent] = formProvider()

  def onPageLoad(storn: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      stampDutyLandTaxService
        .getAgentDetails(storn) map {
          case Some(agentDetails) => Ok(view(form, agentDetails))
          case None               =>
            logger.error(s"[onPageLoad] Failed to retrieve details for agent with storn: $storn")
            Redirect(JourneyRecoveryController.onPageLoad())
      } recover {
        case ex =>
          logger.error("[onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(storn: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      stampDutyLandTaxService.getAgentDetails(storn) flatMap {
        case Some(agentDetails) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentDetails))),
            _ =>
              stampDutyLandTaxService.removeAgentDetails(storn) flatMap {
                case true =>
                  logger.info(s"[onSubmit] Successfully removed agent with storn: $storn")
                  Future.successful(Redirect(HomeController.onPageLoad()))
                case false =>
                  logger.error(s"[onSubmit] Failed to remove agent with storn: $storn")
                  Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
              }
          )
        case None =>
          logger.error(s"[onSubmit] Failed to retrieve details for agent with storn: $storn")
          Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
      } recover {
        case ex =>
          logger.error("[onSubmit] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
  }
}
