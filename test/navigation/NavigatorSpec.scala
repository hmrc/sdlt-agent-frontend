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

import base.SpecBase
import controllers.routes
import pages.*
import models.*
import pages.manageAgents.{AgentAddressPage, AgentContactDetailsPage, AgentNameDuplicateWarningPage, AgentNamePage}

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from AgentNamePage to AgentNameController.onPageLoad(NormalMode)" in {
        navigator.nextPage(AgentNamePage, NormalMode, UserAnswers("id")) mustBe
          controllers.manageAgents.routes.AgentNameController.onPageLoad(NormalMode)
      }

      "must go from AgentNameDuplicateWarningPage to WarningAgentNameController.onPageLoad(NormalMode)" in {
        navigator.nextPage(AgentNameDuplicateWarningPage, NormalMode, UserAnswers("id")) mustBe
          controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(NormalMode)
      }

      "must go from AgentAddressPage to AddressLookupController.onPageLoad(NormalMode)" in {
        navigator.nextPage(AgentAddressPage, NormalMode, UserAnswers("id")) mustBe
          controllers.manageAgents.routes.AddressLookupController.onPageLoad(NormalMode)
      }

      "must go from AgentContactDetailsPage to AgentContactDetailsController.onPageLoad(NormalMode)" in {
        navigator.nextPage(AgentContactDetailsPage, NormalMode, UserAnswers("id")) mustBe
          controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(NormalMode, "")
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad(None)
      }

      "must go from AgentNamePage to AgentNameController.onPageLoad(CheckMode) in Check mode" in {
        navigator.nextPage(AgentNamePage, CheckMode, UserAnswers("id")) mustBe
          controllers.manageAgents.routes.AgentNameController.onPageLoad(CheckMode)
      }

      "must go from AgentNameDuplicateWarningPage to WarningAgentNameController.onPageLoad(CheckMode) in Check mode" in {
        navigator.nextPage(AgentNameDuplicateWarningPage, CheckMode, UserAnswers("id")) mustBe
          controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(CheckMode)
      }
    }
  }
}
