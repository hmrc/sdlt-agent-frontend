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
import utils.manageAgents.AgentDetailsTestUtil

import scala.concurrent.Future

class StartAddAgentControllerSpec extends SpecBase with MockitoSugar with AgentDetailsTestUtil {

  private val service = mock[StampDutyLandTaxService]

  private def postUrl: String = routes.StartAddAgentController.onPageLoad().url
  "StartAddAgentController.onPageLoad" - {

    "must redirect to AgentNameController when the number of agents is below the max" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(getAgentList(MAX_AGENTS - 1)))

      running(application) {
        val request = FakeRequest(GET, postUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.AgentNameController.onPageLoad(mode = models.NormalMode).url
      }
    }

    "must redirect back to AgentOverview (page 1) with a flash when the number of agents is at or above the max" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(getAgentList(MAX_AGENTS)))

      running(application) {
        val request = FakeRequest(GET, postUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.AgentOverviewController.onPageLoad(1).url

        flash(result).get("agentsLimitReached") mustBe Some("true")
      }
    }
    
    "must redirect to JourneyRecoveryController when StampDutyLandTaxService fails unexpectedly" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, postUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
