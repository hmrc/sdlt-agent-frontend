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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, StornRequiredAction}
import forms.manageAgents.AddAnotherAgentFormProvider
import models.NormalMode
import navigation.Navigator
import pages.manageAgents.AgentOverviewPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StampDutyLandTaxService
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PaginationHelper
import views.html.manageAgents.AgentOverviewView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentOverviewController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         stampDutyLandTaxService: StampDutyLandTaxService,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         addAgentFormProvider: AddAnotherAgentFormProvider,
                                         stornRequiredAction: StornRequiredAction,
                                         navigator: Navigator,
                                         view: AgentOverviewView
                                      )(implicit executionContext: ExecutionContext, appConfig:FrontendAppConfig) extends FrontendBaseController with PaginationHelper with I18nSupport with Logging {

  val form: Form[Boolean] = addAgentFormProvider()
  def onPageLoad(paginationIndex: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
    stampDutyLandTaxService
      .getAllAgentDetails(request.storn).map {
        case Nil              => Ok(view(form, None, None, None, paginationIndex))
        case agentDetailsList =>

          generateAgentSummary(paginationIndex, agentDetailsList)
            .fold(
              Redirect(navigator.nextPage(AgentOverviewPage, NormalMode, request.userAnswers))
            ) { summary =>
              val numberOfPages:  Int                = getNumberOfPages(agentDetailsList)
              val pagination:     Option[Pagination] = generatePagination(paginationIndex, numberOfPages)
              val paginationText: Option[String]     = getPaginationInfoText(paginationIndex, agentDetailsList)

              Ok(view(form, Some(summary), pagination, paginationText, paginationIndex))
            }
      } recover {
        case ex =>
          logger.error("[AgentOverviewController][onPageLoad] Unexpected failure", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(paginationIndex: Int):Action[AnyContent] = {
    (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {
          stampDutyLandTaxService.getAllAgentDetails(request.storn).map  {
            case Nil  => BadRequest(view(formWithErrors,None, None, None, paginationIndex))
            case agentDetailsList =>
              generateAgentSummary(paginationIndex, agentDetailsList) match {
                case None => Redirect(navigator.nextPage(AgentOverviewPage, NormalMode, request.userAnswers))
                case Some(summary) =>
                  val numberOfPages: Int = getNumberOfPages(agentDetailsList)
                  val pagination: Option[Pagination] = generatePagination(paginationIndex, numberOfPages)
                  val paginationText: Option[String] = getPaginationInfoText(paginationIndex, agentDetailsList)
                  BadRequest(view(formWithErrors, Some(summary), pagination, paginationText, paginationIndex))
              }
          }recover {
            case ex =>
              logger.error("[AgentOverviewController][onPageLoad] Unexpected failure", ex)
              Redirect(controllers.routes.SystemErrorController.onPageLoad())
          }
        },
        value =>
          if(value){
            Future.successful(Redirect(controllers.manageAgents.routes.StartAddAgentController.onPageLoad()))
          }
          else {
            Future.successful(Redirect(appConfig.managementAtAGlanceUrl))
          }
      )

    }
  }
}
