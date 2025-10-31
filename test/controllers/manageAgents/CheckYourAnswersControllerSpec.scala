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

import base.SpecBase
import controllers.routes
import models.UserAnswers
import pages.manageAgents.StornPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.libs.json.Json
import utils.mangeAgents.AgentDetailsTestUtil
import viewmodels.govuk.SummaryListFluency
import viewmodels.manageAgents.checkAnswers.{AddressSummary, AgentNameSummary, ContactEmailSummary, ContactPhoneNumberSummary}
import views.html.manageAgents.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with AgentDetailsTestUtil {

  private def checkYourAnswersUrl =
    controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad().url

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val testData = Json.obj(
        "agentName" -> "John",
        "agentAddress" -> "123 Road",
        "agentContactDetails" -> Json.obj(
          "contactTelephoneNumber" -> "07123456789",
          "contactEmail" -> "john@example.com"
        )
      )

      val ua = UserAnswers("id", testData).set(StornPage, testStorn).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersUrl)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            list = SummaryListViewModel(
              Seq(
                AgentNameSummary.row(ua)(messages(application)).get,
                AddressSummary.row(ua)(messages(application)).get,
                ContactPhoneNumberSummary.row(ua)(messages(application)).get,
                ContactEmailSummary.row(ua)(messages(application)).get
              )),
            postAction = controllers.manageAgents.routes.SubmitAgentController.onSubmit()
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersUrl)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
