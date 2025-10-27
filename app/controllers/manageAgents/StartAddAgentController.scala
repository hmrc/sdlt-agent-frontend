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

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes.JourneyRecoveryController
import models.NormalMode
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class StartAddAgentController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     stampDutyLandTaxService: StampDutyLandTaxService,
                                   )(implicit appConfig: FrontendAppConfig,
                                     executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val MaxAgents = appConfig.maxNumberOfAgents

  def onSubmit(storn: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      stampDutyLandTaxService.getAllAgentDetails(storn).map { agents =>
        if (agents.size >= MaxAgents) {
          Redirect(controllers.manageAgents.routes.AgentOverviewController.onPageLoad(storn, 1))
            .flashing("agentsLimitReached" -> "true")
        } else {
          Redirect(controllers.manageAgents.routes.AgentNameController.onPageLoad(mode = NormalMode))
        }
      } recover {
        case ex =>
          logger.error("[onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
      }
    }
}
