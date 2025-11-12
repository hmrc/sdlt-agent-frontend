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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import utils.mangeAgents.AgentDetailsTestUtil
import views.html.manageAgents.AgentOverviewView

import scala.concurrent.Future

class AgentOverviewControllerSpec extends SpecBase with MockitoSugar with AgentDetailsTestUtil {

  private val service = mock[StampDutyLandTaxService]

  private def agentOverviewUrl(page: Int) =
    controllers.manageAgents.routes.AgentOverviewController.onPageLoad(page).url

  private def startAddAgentJourneyUrl =
    controllers.manageAgents.routes.StartAddAgentController.onSubmit().url

  private val agents22 = getAgentList(22)

  "AgentOverviewController.onPageLoad" - {

    "must return OK and the correct view for a GET when there are no agents" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetailsLegacy(any())(any()))
        .thenReturn(Future.successful(Nil))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 1))
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AgentOverviewView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            maybeSummaryList   = None,
            pagination         = None,
            paginationInfoText = None,
            postAction         = controllers.manageAgents.routes.StartAddAgentController.onSubmit()
          )(request, messages(application)).toString
      }
    }

    "must return OK and render a summary and pagination when there are agents (valid first page)" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetailsLegacy(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 1))
        val result  = route(application, request).value

        status(result) mustEqual OK
        val body = contentAsString(result)

        body must include("Agent 1")
        body must include("Agent 10")
        body must not include "Agent 11"

        body must include(s"""action="$startAddAgentJourneyUrl"""")

        body must include("pagination")
        body must include("Next")
        body must include("paginationIndex=2")
      }
    }

    "must redirect to page 1 when pagination index is 0 (out of range)" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetailsLegacy(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 0))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agentOverviewUrl(page = 1)
      }
    }

    "must redirect to page 1 when pagination index exceeds the number of pages" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetailsLegacy(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 99))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agentOverviewUrl(page = 1)
      }
    }

    "must redirect to JourneyRecoveryController when StampDutyLandTaxService fails unexpectedly" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetailsLegacy(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 1))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

