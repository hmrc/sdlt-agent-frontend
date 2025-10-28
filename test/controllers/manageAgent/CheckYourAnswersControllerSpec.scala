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

package controllers.manageAgent

import base.SpecBase
import controllers.{manageAgents, routes}
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.inject.bind
import play.api.libs.json.Json
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import viewmodels.manageAgents.checkAnswers.{AddressSummary, AgentNameSummary, ContactEmailSummary, ContactTelephoneNumberSummary}
import views.html.manageAgents.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  def onwardRoute = Call("GET", "/stamp-duty-land-tax-agent/manage-agents/agent-overview")

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val data = Json.obj(
                "agentName" -> "John",
                "addressLookup" -> "123 Road",
                "agentContactDetails" -> Json.obj(
                  "contactTelephoneNumber" -> "07123456789",
                  "contactEmail" -> "john@example.com"
                )
      )

      val ua = UserAnswers("id", data)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, manageAgents.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(
          Seq(
          AgentNameSummary.row(ua)(messages(application)).get,
          AddressSummary.row(ua)(messages(application)).get,
          ContactTelephoneNumberSummary.row(ua)(messages(application)).get,
          ContactEmailSummary.row(ua)(messages(application)).get
        ))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, manageAgents.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, manageAgents.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
