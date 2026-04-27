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
import config.FrontendAppConfig
import forms.manageAgents.AddAnotherAgentFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.StampDutyLandTaxService
import utils.manageAgents.AgentDetailsTestUtil
import views.html.manageAgents.AgentOverviewView

import scala.concurrent.Future

class AgentOverviewControllerSpec
    extends SpecBase
    with MockitoSugar
    with AgentDetailsTestUtil {

  "AgentOverviewController.onPageLoad()" - {

    "must return OK and the correct view for a GET when there are no agents" in new Setup {

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(Nil))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 1))
        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentOverviewView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form = formProvider,
            maybeSummaryList = None,
            pagination = None,
            paginationInfoText = None,
            paginationIndex = 1
          )(request, messages(application)).toString
      }
    }

    "must return OK and render a summary and pagination when there are agents (valid first page)" in new Setup {

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 1))
        val result = route(application, request).value

        status(result) mustEqual OK
        val body = contentAsString(result)

        body must include("Agent 1")
        body must include("Agent 10")
        body must not include "Agent 11"

        body must include(s"""href="${agentOverviewOnSubmitUrl(page = 1)}"""")

        body must include("pagination")
        body must include("Next")
        body must include("paginationIndex=2")
      }
    }

    "must redirect to page 1 when pagination index is 0 (out of range)" in new Setup {

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 0))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agentOverviewUrl(page = 1)
      }
    }

    "must redirect to page 1 when pagination index exceeds the number of pages" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 99))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agentOverviewUrl(page = 1)
      }
    }

    "must redirect to JourneyRecoveryController when StampDutyLandTaxService fails unexpectedly" in new Setup {

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, agentOverviewUrl(page = 1))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.routes.SystemErrorController
          .onPageLoad()
          .url
      }
    }
  }

  "AgentOverviewController.onSubmit()" - {
    "must return BadRequest and the correct view for a POST when there are no agents with the form has errors" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(Nil))

      running(application) {
        val request = FakeRequest(POST, agentOverviewOnSubmitUrl(page = 1))
          .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentOverviewView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            form = formWithErrors,
            maybeSummaryList = None,
            pagination = None,
            paginationInfoText = None,
            paginationIndex = 1
          )(request, messages(application)).toString
      }
    }
    "must return BadRequest and render a summary and pagination when there are agents (valid first page) when form has errors" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(POST, agentOverviewOnSubmitUrl(page = 1))
          .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val body = contentAsString(result)

        body must include("Agent 1")
        body must include("Agent 10")
        body must not include "Agent 11"

        body must include(s"""href="${agentOverviewOnSubmitUrl(page = 1)}"""")

        body must include("pagination")
        body must include("Next")
        body must include("paginationIndex=2")
      }
    }
    "must redirect to StartAddAgentController when user select yes(true) option" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(POST, agentOverviewOnSubmitUrl(page = 1))
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual startAddAgentRouteUrl
      }
    }
    "must redirect to managementUrl when user select no(false) option" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      when(mockConfig.managementAtAGlanceUrl)
        .thenReturn(testManagementHomePageUrl)

      override val application: Application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service),
            bind[FrontendAppConfig].toInstance(mockConfig)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, agentOverviewOnSubmitUrl(page = 12))
          .withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual testManagementHomePageUrl
      }
    }
    "must redirect to page 1 when pagination index is 0 (out of range)" in new Setup {

      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewOnSubmitUrl(page = 0))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agentOverviewOnSubmitUrl(page =
          1
        )
      }
    }

    "must redirect to page 1 when pagination index exceeds the number of pages" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.successful(agents22))

      running(application) {
        val request = FakeRequest(GET, agentOverviewOnSubmitUrl(page = 99))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual agentOverviewOnSubmitUrl(page =
          1
        )
      }
    }
    "must redirect to JourneyRecoveryController when StampDutyLandTaxService fails unexpectedly" in new Setup {
      when(service.getAllAgentDetails(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(POST, agentOverviewOnSubmitUrl(page = 1))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.routes.SystemErrorController
          .onPageLoad()
          .url
      }
    }
  }

  trait Setup {

    val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

    val application: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[StampDutyLandTaxService].toInstance(service))
        .build()

    val formProvider = new AddAnotherAgentFormProvider()()

    val formWithErrors: Form[Boolean] =
      formProvider.bind(Map.empty[String, String])

    val testManagementHomePageUrl = "http://localhost:1234/test-management-url"

    val paginationIndex: Int = 1

    def agentOverviewUrl(page: Int): String =
      controllers.manageAgents.routes.AgentOverviewController
        .onPageLoad(page)
        .url

    def agentOverviewOnSubmitUrl(page: Int): String =
      controllers.manageAgents.routes.AgentOverviewController.onSubmit(page).url

    def startAddAgentRouteUrl: String =
      controllers.manageAgents.routes.StartAddAgentController.onPageLoad().url

    val agents22 = getAgentList(22)

  }
}
