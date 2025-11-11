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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PaginationHelper
import views.html.manageAgents.AgentOverviewView
import controllers.routes.JourneyRecoveryController
import play.api.Logging
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import controllers.manageAgents.routes.*
import javax.inject.{Inject, Singleton}
import models.NormalMode
import navigation.Navigator
import pages.manageAgents.AgentOverviewPage

import scala.concurrent.ExecutionContext

@Singleton
class AgentOverviewController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        stampDutyLandTaxService: StampDutyLandTaxService,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        stornRequiredAction: StornRequiredAction,
                                                navigator: Navigator,
                                        view: AgentOverviewView
                                      )(implicit executionContext: ExecutionContext) extends FrontendBaseController with PaginationHelper with I18nSupport with Logging {

  def onPageLoad(paginationIndex: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>

    val postAction: Call = StartAddAgentController.onSubmit()

    stampDutyLandTaxService
      .getAllAgentDetails(request.storn).map {
        case Nil              => Ok(view(None, None, None, postAction))
        case agentDetailsList =>

          generateAgentSummary(paginationIndex, agentDetailsList)
            .fold(
              Redirect(navigator.nextPage(AgentOverviewPage, NormalMode, request.userAnswers))
            ) { summary =>

              val numberOfPages:  Int                = getNumberOfPages(agentDetailsList)
              val pagination:     Option[Pagination] = generatePagination(paginationIndex, numberOfPages)
              val paginationText: Option[String]     = getPaginationInfoText(paginationIndex, agentDetailsList)

              Ok(view(Some(summary), pagination, paginationText, postAction))
            }
      } recover {
        case ex =>
          logger.error("[AgentOverviewController][onPageLoad] Unexpected failure", ex)
          Redirect(JourneyRecoveryController.onPageLoad())
    }
  }
}
