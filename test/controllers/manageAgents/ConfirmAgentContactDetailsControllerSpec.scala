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
import forms.manageAgents.ConfirmAgentContactDetailsFormProvider
import models.manageAgents.ConfirmAgentContactDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.AgentNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.manageAgents.ConfirmAgentContactDetailsView

import scala.concurrent.Future

class ConfirmAgentContactDetailsControllerSpec extends SpecBase with MockitoSugar with I18nSupport {
  val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value)).build()

  def noOnwardRoute = Call("GET", "/stamp-duty-land-tax-agent/agent-details/check-your-answers")

  def yesOnwardRoute = Call("GET", "/stamp-duty-land-tax-agent/agent-details/agent-contact-details")

  val messagesApi = application.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  lazy val confirmAgentContactDetailsRoute = controllers.manageAgents.routes.ConfirmAgentContactDetailsController.onPageLoad().url

  private val agentName = "null"
  val formProvider = new ConfirmAgentContactDetailsFormProvider()
  private val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)(messages)

  "ConfirmAgentContactDetailsController" - {

    "must return OK and the correct view for a GET" in {

      running(application) {
        val request = FakeRequest(GET, confirmAgentContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmAgentContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, agentName)(request, messages).toString
      }
    }

    "must redirect to the Journey Recovery page for a GET when agent details are not found" in  {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmAgentContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmAgentContactDetailsView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the agent contact details page when valid YES option is submitted" in {
      val mockSessionRepo = mock[SessionRepository]
      when(mockSessionRepo.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepo))
        .build()

      val request = FakeRequest(POST, confirmAgentContactDetailsRoute)
        .withFormUrlEncodedBody(("value", ConfirmAgentContactDetails.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual yesOnwardRoute.url

    }

    "must redirect to the agent contact details page when valid NO option is submitted" in {
      val mockSessionRepo = mock[SessionRepository]
      when(mockSessionRepo.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepo))
        .build()

      val request = FakeRequest(POST, confirmAgentContactDetailsRoute)
        .withFormUrlEncodedBody(("value", ConfirmAgentContactDetails.values.last.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual noOnwardRoute.url

    }



  }

}
