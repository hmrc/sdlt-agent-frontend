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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages.*
import models.*
import pages.manageAgents.{AgentAddressDetails, AgentCheckYourAnswersPage, AgentContactDetailsPage, AgentNameDuplicateWarningPage, AgentNamePage, AgentOverviewPage}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case AgentNamePage                 => _ => controllers.manageAgents.routes.AgentNameController.onPageLoad(NormalMode)
    case AgentNameDuplicateWarningPage => _ => controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(NormalMode)
    case AgentAddressDetails           => _ => controllers.manageAgents.routes.AddressLookupController.onPageLoad(NormalMode)
    case AgentContactDetailsPage       => _ => controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(NormalMode)
    case AgentCheckYourAnswersPage     => _ => controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad()
    case AgentOverviewPage             => _ => controllers.manageAgents.routes.AgentOverviewController.onPageLoad(1)
    case _                             => _ =>                          routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case AgentNamePage                 => _ => controllers.manageAgents.routes.AgentNameController.onPageLoad(CheckMode)
    case AgentNameDuplicateWarningPage => _ => controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(CheckMode)
    case _                             => _ => controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
