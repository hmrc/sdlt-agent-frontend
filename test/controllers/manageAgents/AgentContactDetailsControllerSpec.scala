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
import navigation.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.AgentNamePage
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.manageAgents.AgentContactDetailsView

import scala.concurrent.Future

class AgentContactDetailsControllerSpec
    extends SpecBase
    with MockitoSugar
    with I18nSupport {
  val application = applicationBuilder(userAnswers =
    Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value)
  ).build()

  val messagesApi = application.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  def onwardRoute =
    Call("GET", "/stamp-duty-land-tax-agent/agent-details/check-your-answers")

  val formProvider = new AgentContactDetailsFormProvider()
  lazy val AgentContactDetailsRoute =
    controllers.manageAgents.routes.AgentContactDetailsController
      .onPageLoad(NormalMode)
      .url

  private val agentName = "null"
  private val form = formProvider(agentName)(messages)

  "AgentContactDetails Controller" - {

    "must return OK and render the view for a GET" in {

      val request = FakeRequest(GET, AgentContactDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AgentContactDetailsView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, NormalMode, agentName)(
        request,
        messages
      ).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepo = mock[SessionRepository]
      when(mockSessionRepo.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value)
      )
        .overrides(bind[SessionRepository].toInstance(mockSessionRepo))
        .build()

      val request = FakeRequest(
        POST,
        routes.AgentContactDetailsController.onSubmit(NormalMode).url
      )
        .withFormUrlEncodedBody(
          "phone" -> "07700900982",
          "email" -> "test@gov.uk"
        )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value)
      ).build()

      val request = FakeRequest(
        POST,
        routes.AgentContactDetailsController.onSubmit(NormalMode).url
      )
        .withFormUrlEncodedBody("phone" -> "abcdef", "email" -> "bad-email")

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
    }

    "must redirect to journey recovery if no data is found on AgentContactDetails page" in {

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
        val request = FakeRequest(POST, AgentContactDetailsRoute)
          .withFormUrlEncodedBody(("phone", ""))
          .withFormUrlEncodedBody(("email", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
  }
}
