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
import models.AgentDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StampDutyLandTaxService

import scala.concurrent.Future

class StartAddAgentControllerSpec extends SpecBase with MockitoSugar {

  private val storn = "STN001"

  private val service = mock[StampDutyLandTaxService]

  private def postUrl: String =
    routes.StartAddAgentController.onSubmit(storn).url

  private def agent(i: Int): AgentDetails =
    AgentDetails(
      storn         = "STN001",
      name          = s"Agent $i",
      houseNumber   = "64",
      addressLine1  = "Zoo Lane",
      addressLine2  = None,
      addressLine3  = "Lazy Town",
      addressLine4  = None,
      postcode      = Some("SW44GFS"),
      phoneNumber   = "0543534534543",
      emailAddress  = "agent@example.com",
      agentId       = "AN001",
      isAuthorised  = 1
    )

  private def agents(n: Int): List[AgentDetails] = (1 to n).map(agent).toList

  private val Max = 25

  "StartAddAgentController.onSubmit" - {

    "must redirect to AgentNameController when the number of agents is below the max" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents(Max - 1)))

      running(application) {
        val request = FakeRequest(POST, postUrl)
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
        .thenReturn(Future.successful(agents(Max)))

      running(application) {
        val request = FakeRequest(POST, postUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.AgentOverviewController.onPageLoad(storn, 1).url

        flash(result).get("agentsLimitReached") mustBe Some("true")
      }
    }

    "must redirect to Journey Recovery when no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, postUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
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
        val request = FakeRequest(POST, postUrl)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
