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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import forms.NoSessionDataFormProvider
import models.NoSessionData
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.NoSessionDataView


class NoSessionDataControllerSpec extends SpecBase with MockitoSugar {

  private lazy val noSessionDataRoute = routes.NoSessionDataController.onPageLoad().url
  private lazy val noSessionDataSubmitRoute = routes.NoSessionDataController.onSubmit().url

  val formProvider = new NoSessionDataFormProvider()
  val form: Form[NoSessionData] = formProvider()

  "NoSessionDataController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, noSessionDataRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[NoSessionDataView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString()
      }
    }

    "must redirect to sdlt-filing-service when FileNewReturn is selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, noSessionDataSubmitRoute)
          .withFormUrlEncodedBody(("value", NoSessionData.FileNewReturn.toString))

        val result = route(application, request).value
        val config = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual config.startNewReturnUrl
      }
    }

    "must redirect to sdlt-management-service when ManageStampTaxes is selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, noSessionDataSubmitRoute)
          .withFormUrlEncodedBody(("value", NoSessionData.ManageStampTaxes.toString))

        val result = route(application, request).value
        val config = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual config.managementAtAGlanceUrl
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, noSessionDataSubmitRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[NoSessionDataView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
