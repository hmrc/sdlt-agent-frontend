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
import connectors.RateLimitedAllowListConnector
import controllers.actions.IdentifierAction
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import utils.LoggingUtil
import pages.manageAgents.{AgentOverviewPage, StornPage}

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartAddAgentController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     stampDutyLandTaxService: StampDutyLandTaxService,
                                     sessionRepository: SessionRepository,
                                     navigator: Navigator,
                                     rateLimitedAllowListConnector: RateLimitedAllowListConnector
                                   )(implicit appConfig: FrontendAppConfig,
                                     executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport with LoggingUtil {

  private val MAX_AGENTS = appConfig.maxNumberOfAgents

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    checkAllowList(appConfig.useRateLimitedAllowList, appConfig.splitterAllowListName, request.storn)
      .flatMap {
        case false =>
          Future.successful(Redirect(appConfig.legacySdltServiceUrl(request)))

        case true =>
          stampDutyLandTaxService
            .getAllAgentDetails(request.storn)
            .flatMap {
              case agents if agents.size >= MAX_AGENTS =>

                val userAnswers = UserAnswers(id = request.userId)

                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(StornPage, request.storn))
                               _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(AgentOverviewPage, NormalMode, updatedAnswers))
                  .flashing("agentsLimitReached" -> "true")

              case _ =>

                val emptiedUserAnswers = UserAnswers(id = request.userId)

                for {
                  updatedAnswers <- Future.fromTry(emptiedUserAnswers.set(StornPage, request.storn))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.manageAgents.routes.BeforeYouStartController.onPageLoad())
            }
      } recover {
      case ex =>
        logger.error("[StartAddAgentController][onPageLoad] Unexpected failure", ex)
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  private def checkAllowList(useRateLimitedAllowList: Boolean, allowListName: String, storn: String)(
    implicit hc: HeaderCarrier
  ): Future[Boolean] =
    if (useRateLimitedAllowList) {
      rateLimitedAllowListConnector.checkAllowList(allowListName, storn)
    } else {
      Future.successful(true)
    }
}
