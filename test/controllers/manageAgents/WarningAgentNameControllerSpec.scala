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
import forms.manageAgents.AgentNameFormProvider
import models.NormalMode
import models.UserAnswers
import navigation.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.AgentNamePage
import pages.manageAgents.StornPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.StampDutyLandTaxService
import utils.manageAgents.AgentDetailsTestUtil
import views.html.manageAgents.WarningAgentNameView

import scala.concurrent.Future

class WarningAgentNameControllerSpec
    extends SpecBase
    with MockitoSugar
    with AgentDetailsTestUtil {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

  val formProvider = new AgentNameFormProvider()
  val form = formProvider()

  "AgentNameController" - {
    "must return OK and the correct view for a GET in NormalMode" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()
      running(application) {
        val request = FakeRequest(
          GET,
          AgentNamePageWarningUtils.WarningAgentNameRequestRoute(NormalMode).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[WarningAgentNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(StornPage, testStorn)
        .success
        .value
        .set(AgentNamePage, "Test Agent Name")
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          AgentNamePageWarningUtils.WarningAgentNameRequestRoute(NormalMode).url
        )

        val view = application.injector.instanceOf[WarningAgentNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill("Test Agent Name"),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            AgentNamePageWarningUtils
              .WarningAgentNameRequestRoute(NormalMode)
              .url
          )
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WarningAgentNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(
          GET,
          AgentNamePageWarningUtils.WarningAgentNameRequestRoute(NormalMode).url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            AgentNamePageWarningUtils
              .WarningAgentNameRequestRoute(NormalMode)
              .url
          )
            .withFormUrlEncodedBody(("value", "Test Agent Name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(
                AgentNamePageWarningUtils.onwardRoute(NormalMode)
              )
            ),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            AgentNamePageWarningUtils
              .WarningAgentNameRequestRoute(NormalMode)
              .url
          )
            .withFormUrlEncodedBody("value" -> "Unique Agent Name")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AgentNamePageWarningUtils
          .onwardRoute(NormalMode)
          .url
        verify(mockSessionRepository, times(1)).set(any[UserAnswers])
      }
    }
  }
}
