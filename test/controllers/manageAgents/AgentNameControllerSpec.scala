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
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.StampDutyLandTaxService
import utils.manageAgents.AgentDetailsTestUtil
import views.html.manageAgents.AgentNameView

import scala.concurrent.Future

class AgentNameControllerSpec
    extends SpecBase
    with MockitoSugar
    with AgentDetailsTestUtil {

  val formProvider = new AgentNameFormProvider()
  val form: Form[String] = formProvider()

  val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

  "AgentNameController" - {
    "must return OK and the correct view for a GET in NormalMode" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, agentNameRequestRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AgentNamePage, "Test Agent Name")
        .success
        .value
        .set(StornPage, testStorn)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, agentNameRequestRoute)

        val view = application.injector.instanceOf[AgentNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill("Test Agent Name"),
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the warning page when Agent name already exists" in {

      when(service.isDuplicate(any(), any())(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service),
            bind[Navigator].toInstance(
              new FakeNavigator(
                AgentNamePageUtils.agentNameDuplicateNameRoute(NormalMode)
              )
            )
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, agentNameRequestRoute)
          .withFormUrlEncodedBody(("value", "Duplicate Agent Name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AgentNamePageUtils
          .agentNameDuplicateNameRoute(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentNameRequestRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentNameView]

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
        val request = FakeRequest(GET, agentNameRequestRoute)

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
          FakeRequest(POST, agentNameRequestRoute)
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
    "must redirect to the next page (address) when Agent name does not already exist" in {

      when(service.isDuplicate(any(), any())(any()))
        .thenReturn(Future.successful(false))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service),
            bind[Navigator].toInstance(
              new FakeNavigator(
                AgentNamePageUtils.agentNameOnwardRoute(NormalMode)
              )
            )
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, agentNameRequestRoute)
          .withFormUrlEncodedBody(("value", "Unique Agent Name"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AgentNamePageUtils
          .agentNameOnwardRoute(NormalMode)
          .url
      }
    }
  }
}
