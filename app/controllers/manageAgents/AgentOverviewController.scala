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

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PaginationHelper
import views.html.manageAgents.AgentOverviewView
import controllers.manageAgents.routes.*
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

import scala.concurrent.ExecutionContext

class AgentOverviewController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        stampDutyLandTaxService: StampDutyLandTaxService,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        view: AgentOverviewView
                                      )(implicit executionContext: ExecutionContext) extends FrontendBaseController with PaginationHelper with I18nSupport {

  def onPageLoad(storn: String, paginationIndex: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    val postAction: Call = controllers.manageAgents.routes.StartAddAgentController.onSubmit(storn)

    stampDutyLandTaxService
      .getAllAgentDetails(storn).map {
        case Nil              => Ok(view(None, None, None, postAction))
        case agentDetailsList =>

          val numberOfPages:            Int                = getNumberOfPages(agentDetailsList)
          val pagination:               Option[Pagination] = generatePagination(storn, paginationIndex, numberOfPages)
          val paginationText:           Option[String]     = getPaginationInfoText(paginationIndex, agentDetailsList)

          generateAgentSummary(paginationIndex, agentDetailsList)
            .fold(
              Redirect(AgentOverviewController.onPageLoad(storn, 1))
            ) { summary =>
              Ok(view(Some(summary), pagination, paginationText, postAction))
            }
      }
  }
}
