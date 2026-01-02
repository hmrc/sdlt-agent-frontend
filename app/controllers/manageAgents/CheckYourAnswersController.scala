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
import models.requests.{CreatePredefinedAgentRequest, UpdatePredefinedAgent}
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.manageAgents.{AgentOverviewPage, AgentReferenceNumberPage, StornPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logError
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
                                            sessionRepository: SessionRepository,
                                            navigator: Navigator,
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport  with UserAnswersHelper {

  def onPageLoad(agentReferenceNumber: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequired).async {
    implicit request =>

      val storedArn = request.userAnswers.get(AgentReferenceNumberPage)

      val postAction:Call = controllers.manageAgents.routes.CheckYourAnswersController.onSubmit(agentReferenceNumber)

      def getSummaryListRows(userAnswers: UserAnswers) = SummaryListViewModel(
        rows = Seq(
          AgentNameSummary.row(userAnswers),
          AddressSummary.row(userAnswers),
          ContactPhoneNumberSummary.row(userAnswers),
          ContactEmailSummary.row(userAnswers)
        ).flatten
      )

      (storedArn, agentReferenceNumber) match {
        case (Some(storedArn), Some(paramArn)) if storedArn == paramArn =>
          logError(s"[CheckYourAnswersController][onPageLoad] storedArn: ${storedArn}, paramArn: ${paramArn}")
          Future.successful(Ok(view(getSummaryListRows(request.userAnswers), postAction)))
        case (_, Some(paramArn)) =>
          stampDutyLandTaxService.getAgentDetails(request.storn, paramArn) flatMap {
            case Some(agentDetails) =>
              updateUserAnswers(agentDetails)
                .fold({ error =>
                  logError(s"[CheckYourAnswersController][onPageLoad] Failed to build UA: ${error.getMessage}")
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }, { userAnswers =>
                  sessionRepository.set(userAnswers).map { _ =>
                    Ok(view(getSummaryListRows(userAnswers), postAction))
                  }
                })

            case None =>
              logError(s"[CheckYourAnswersController][onPageLoad]: Failed to retrieve details for agent with agentReferenceNumber: $paramArn")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          } recover {
            case ex =>
              logError(s"[CheckYourAnswersController][onPageLoad] Unexpected failure: ${ex.getMessage}")
              Redirect(controllers.routes.SystemErrorController.onPageLoad())
          }
        case _ =>
          logError(s"[CheckYourAnswersController][onPageLoad] ReloadPage from existing data from session}")
          Future.successful(Ok(view(getSummaryListRows(request.userAnswers), postAction)))
      }

  }


  def onSubmit(agentReferenceNumber: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequired).async {
    implicit request =>
      agentReferenceNumber match {
        case None =>
          request.userAnswers.data.asOpt[CreatePredefinedAgentRequest] match {
            case None =>
              logError("[CheckYourAnswersController][onSubmit] Failed to construct AgentDetailsRequest")
              Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
            case Some(createPredefinedAgentRequest) =>
              val emptiedUserAnswers = UserAnswers(request.userId)
              (for {
                _ <- stampDutyLandTaxService.submitAgentDetails(createPredefinedAgentRequest)
                updatedAnswers <- Future.fromTry(emptiedUserAnswers.set(StornPage, request.storn))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(AgentOverviewPage, NormalMode, updatedAnswers)
              ).flashing("agentCreated" -> createPredefinedAgentRequest.agentName)
                ).recover {
                case ex =>
                  logError(s"[CheckYourAnswersController][onSubmit] Unexpected failure: ${ex.getMessage}")
                  Redirect(controllers.routes.SystemErrorController.onPageLoad())
              }
          }
        case Some(arn) =>
          request.userAnswers.data.asOpt[UpdatePredefinedAgent] match {
            case None =>
              logError("[CheckYourAnswersController][onSubmit Update] Failed to construct UpdatePredefinedAgent")
              Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
            case Some(updatePredefinedAgent) =>
              val updated = updatePredefinedAgent.copy(agentResourceReference = Some(arn))
              logError(s"[CheckYourAnswersController][onSubmit] updatedAgent: ${updated}")
              (for {
                _ <- stampDutyLandTaxService.updateAgentDetails(updated)
                updatedAnswers <- Future.fromTry(request.userAnswers.set(StornPage, request.storn))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect({
                navigator.nextPage(AgentOverviewPage, NormalMode, updatedAnswers)
              }).flashing("agentUpdated" -> updatePredefinedAgent.agentName)
                ).recover {
                case ex =>
                  logError(s"[CheckYourAnswersController][onSubmit update] Unexpected failure: ${ex.getMessage}")
                  Redirect(controllers.routes.SystemErrorController.onPageLoad())
              }

          }
      }

  }
}

