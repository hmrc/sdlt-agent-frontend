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
import forms.manageAgents.AgentContactDetailsFormProvider
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.StampDutyLandTaxService
import views.html.manageAgents.AgentContactDetailsView
import utils.mangeAgents.AgentDetailsTestUtil

import scala.concurrent.Future

class AgentContactDetailsControllerSpec extends SpecBase with MockitoSugar with AgentDetailsTestUtil {


  def onwardRoute = Call("GET", "/stamp-duty-land-tax-agent/manage-agents/check-your-answers")

  private val agentReferenceNumber: String = "ARN001"
  val formProvider = new AgentContactDetailsFormProvider()
  val form = formProvider(testAgentDetails)
  val postAction: Call = controllers.manageAgents.routes.AgentContactDetailsController.onSubmit(NormalMode, agentReferenceNumber)
  val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

  lazy val AgentContactDetailsRoute: String = controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(NormalMode, agentReferenceNumber).url


  "AgentContactDetails Controller" - {

    "must return OK and the correct view for a GET request" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request = FakeRequest(GET, AgentContactDetailsRoute)

        val view = application.injector.instanceOf[AgentContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, postAction, testAgentDetails)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request =
          FakeRequest(POST, AgentContactDetailsRoute)
            .withFormUrlEncodedBody(("phone", "0123456789"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[StampDutyLandTaxService].toInstance(service)
        ).build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request =
          FakeRequest(POST, AgentContactDetailsRoute)
            .withFormUrlEncodedBody(("phone", "0123456789101112"))

        val boundForm = form.bind(Map("phone" -> "0123456789101112"))

        val view = application.injector.instanceOf[AgentContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, postAction, testAgentDetails)(request, messages(application)).toString
      }
    }

    "must navigate to Check your answers for a POST if no data is found on AgentContactDetails page" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, AgentContactDetailsRoute)
          .withFormUrlEncodedBody(("phone", ""))
          .withFormUrlEncodedBody(("email", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad(None).url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request = FakeRequest(GET, AgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request = FakeRequest(POST, AgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page for a GET when agent details are not found" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, AgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page for a POST when agent details are not found" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(POST, AgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page for a GET when StampDutyLandTaxService fails unexpectedly " in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      running(application) {
        val request = FakeRequest(GET, AgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page for a POST when StampDutyLandTaxService fails unexpectedly " in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      running(application) {
        val request = FakeRequest(POST, AgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
