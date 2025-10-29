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
import forms.manageAgents.AgentNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.AgentNamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.StampDutyLandTaxService
import views.html.manageAgent.AgentNameView

import scala.concurrent.Future

class AgentNameControllerSpec extends SpecBase with MockitoSugar {

  val storn: String = "STN001"

  lazy val AgentNameRequestRoute: String = controllers.manageAgents.routes.AgentNameController.onPageLoad(NormalMode, storn).url

  val formProvider = new AgentNameFormProvider()
  val form = formProvider()

  val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  def warningRoute: Call = controllers.manageAgents.routes.WarningAgentNameController.onPageLoad(NormalMode)

  "AgentNameController" - {
    "must return OK and the correct view for a GET in NormalMode" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, AgentNameRequestRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, storn)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(AgentNamePage, "Test Agent Name").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, AgentNameRequestRoute)

        val view = application.injector.instanceOf[AgentNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("Test Agent Name"), NormalMode, storn)(request, messages(application)).toString
      }
    }
    
    "must redirect to the warning page when Agent name already exists" in {

      when(service.isDuplicate(any(),any())(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service),
            bind[Navigator].toInstance(new FakeNavigator(warningRoute))
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, AgentNameRequestRoute)
          .withFormUrlEncodedBody(("value", "Duplicate Agent Name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual warningRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, AgentNameRequestRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, storn)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, AgentNameRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, AgentNameRequestRoute)
            .withFormUrlEncodedBody(("value", "Test Agent Name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
