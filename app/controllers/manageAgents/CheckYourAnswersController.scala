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
import models.UserAnswers
import models.manageAgents.AgentContactDetails
import models.responses.addresslookup.{Address, JourneyResultAddressModel}
import pages.manageAgents.{AgentAddressPage, AgentContactDetailsPage, AgentNameDuplicateWarningPage, AgentNamePage}
import play.api.Logging

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.manageAgents.UserAnswersHelper
import viewmodels.govuk.summarylist.*
import viewmodels.manageAgents.checkAnswers.*
import views.html.manageAgents.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            stornRequired: StornRequiredAction,
                                            sessionRepository: SessionRepository,
                                            stampDutyLandTaxService: StampDutyLandTaxService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging with UserAnswersHelper {

  def onPageLoad(agentReferenceNumber: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequired).async {
    implicit request =>

      // TODO: This is a dummy postAction call -> must be replaced with an actual call to the BE
      val postAction: Call = controllers.manageAgents.routes.SubmitAgentController.onSubmit()

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
                .fold ({ error =>
                  logger.error(s"[CheckYourAnswersController][onPageLoad] Failed to build UA: ${error.getMessage}", error)
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }, { userAnswers =>
                  sessionRepository.set(userAnswers).map { _ =>
                    Ok(view(getSummaryListRows(userAnswers), postAction))
                  }
                })

            case None =>
              logger.error(s"[CheckYourAnswersController][onPageLoad]: Failed to retried details for agent with agentReferenceNumber: $arn")
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          } recover {
            case ex =>
              logger.error("[CheckYourAnswersController][onPageLoad] Unexpected failure", ex)
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }
}
