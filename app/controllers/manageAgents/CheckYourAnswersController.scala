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
import models.{AgentDetailsRequest, NormalMode, UserAnswers}
import navigation.Navigator
import pages.manageAgents.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.manageAgents.UserAnswersHelper
import viewmodels.govuk.summarylist.*
import viewmodels.manageAgents.checkAnswers.*
import views.html.manageAgents.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            stornRequired: StornRequiredAction,
                                            navigator: Navigator,
                                            sessionRepository: SessionRepository,
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging with UserAnswersHelper {

  def onPageLoad(agentReferenceNumber: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequired).async {
    implicit request =>

      val postAction: Call = controllers.manageAgents.routes.CheckYourAnswersController.onSubmit(agentReferenceNumber)
      def getSummaryListRows(userAnswers: UserAnswers) = SummaryListViewModel(
        rows = Seq(
          AgentNameSummary.row(userAnswers),
          AddressSummary.row(userAnswers),
          ContactPhoneNumberSummary.row(userAnswers),
          ContactEmailSummary.row(userAnswers)
        ).flatten
      )

      agentReferenceNumber match {
        case None =>
          Future.successful(Ok(view(getSummaryListRows(request.userAnswers), postAction)))
        case Some(arn) =>
          stampDutyLandTaxService.getAgentDetails(request.storn, arn) flatMap {
            case Some(agentDetails) =>

              updateUserAnswers(agentDetails)
                .fold({ error =>
                  logger.error(s"[CheckYourAnswersController][onPageLoad] Failed to build UA: ${error.getMessage}", error)
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }, { userAnswers =>
                  sessionRepository.set(userAnswers).map { _ =>
                    Ok(view(getSummaryListRows(userAnswers), postAction))
                  }
                })

            case None =>
              logger.error(s"[CheckYourAnswersController][onPageLoad]: Failed to retrieve details for agent with agentReferenceNumber: $arn")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          } recover {
            case ex =>
              logger.error("[CheckYourAnswersController][onPageLoad] Unexpected failure", ex)
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

  def onSubmit(agentReferenceNumber: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequired).async {
    implicit request =>
      agentReferenceNumber match {
        case None =>
          print(s"Payload structure:${request.userAnswers.data}")
          request.userAnswers.data.asOpt[AgentDetailsRequest] match {
            case None =>
              logger.error("[CheckYourAnswersController][onSubmit] Failed to construct AgentDetailsRequest")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            case Some(agentDetails) =>
              print(s"Agent Details Structure $agentDetails")
              stampDutyLandTaxService.submitAgentDetails(agentDetails) map {
                _ => Redirect(navigator.nextPage(AgentOverviewPage, NormalMode, request.userAnswers))
              } recover {
                case ex =>
                  logger.error("[CheckYourAnswersController][onSubmit] Unexpected failure", ex)
                  Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          }
        case Some(arn) =>
          logger.warn("[CheckYourAnswersController][onSubmit] Duplicate submission attempt detected; blocking progression")
          //TODO: Need to add a real backend call to update a pre-existing agent
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }


  }
}

