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
import views.html.manageAgents.AgentOverviewView

import scala.concurrent.Future

class AgentOverviewControllerSpec extends SpecBase with MockitoSugar {

  val storn = "STN001"

  private val service = mock[StampDutyLandTaxService]

  private def agentOverviewUrl(page: Int) =
    controllers.manageAgents.routes.AgentOverviewController.onPageLoad(storn, page).url

  private def startAddAgentJourneyUrl =
    controllers.manageAgents.routes.StartAddAgentController.onSubmit(storn).url

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

  private val agents22 = (1 to 22).map(agent)

  "AgentOverviewController.onPageLoad" - {

    "must return OK and the correct view for a GET when there are no agents" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetails(any())(any()))
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
            postAction         = controllers.manageAgents.routes.StartAddAgentController.onSubmit(storn)
          )(request, messages(application)).toString
      }
    }

    "must return OK and render a summary and pagination when there are agents (valid first page)" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22.toList))

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

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22.toList))

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

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22.toList))

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

      when(service.getAllAgentDetails(any())(any()))
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

