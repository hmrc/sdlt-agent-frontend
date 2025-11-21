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
import navigation.{FakeNavigator, Navigator}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import controllers.routes
import forms.manageAgents.RemoveAgentFormProvider
import models.manageAgents.RemoveAgent
import models.responses.organisation.CreatedAgent
import play.api.data.Form
import play.api.mvc.Call
import services.StampDutyLandTaxService
import views.html.manageAgents.RemoveAgentView
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import utils.mangeAgents.AgentDetailsTestUtil

import scala.concurrent.Future

class RemoveAgentControllerSpec extends SpecBase with MockitoSugar with AgentDetailsTestUtil with GuiceOneAppPerSuite {

  private val agentReferenceNumber: String = "ARN001"

  def agentNameOnwardRoute: Call = controllers.manageAgents.routes.AgentOverviewController.onPageLoad(1)

  lazy val postAction: Call = controllers.manageAgents.routes.RemoveAgentController.onSubmit(agentReferenceNumber)

  lazy val removeAgentRequestRoute: String = controllers.manageAgents.routes.RemoveAgentController.onPageLoad(agentReferenceNumber).url

  val formProvider = new RemoveAgentFormProvider()
  
  val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

  implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])

  val testAgentResponse: CreatedAgent = CreatedAgent(
    storn = testStorn,
    agentId = None,
    name = "Harborview Estates",
    houseNumber = None,
    address1 = "Queensway",
    address2 = None,
    address3 = Some("Birmingham"),
    address4 = None,
    postcode = Some("B2 4ND"),
    phone = "01214567890",
    email = "info@harborviewestates.co.uk",
    dxAddress = None,
    agentResourceReference = agentReferenceNumber
  )


  val form: Form[RemoveAgent] = formProvider(testAgentResponse)

  "RemoveAgentController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request = FakeRequest(GET, removeAgentRequestRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, postAction, testAgentDetails)(request, messages(application)).toString
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
        val request = FakeRequest(GET, removeAgentRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page for a GET when StampDutyLandTaxService fails unexpectedly" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, removeAgentRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
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
        val request = FakeRequest(POST, removeAgentRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
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
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(POST, removeAgentRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(agentNameOnwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      when(service.removeAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(true))

      running(application) {
        val request =
          FakeRequest(POST, removeAgentRequestRoute)
            .withFormUrlEncodedBody(("value", RemoveAgent.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manageAgents.routes.AgentOverviewController.onPageLoad(1).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      when(service.getAgentDetails(any(), any())(any()))
        .thenReturn(Future.successful(Some(testAgentDetails)))

      running(application) {
        val request =
          FakeRequest(POST, removeAgentRequestRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RemoveAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, postAction, testAgentDetails)(request, messages(application)).toString
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
        val request = FakeRequest(GET, removeAgentRequestRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAgentRequestRoute)
            .withFormUrlEncodedBody(("value", RemoveAgent.values.head.toString))

        when(service.getAgentDetails(any(), any())(any()))
          .thenReturn(Future.successful(Some(testAgentDetails)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if stampDutyLandTaxService fails to remove the agent" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[StampDutyLandTaxService].toInstance(service)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAgentRequestRoute)
            .withFormUrlEncodedBody(("value", RemoveAgent.values.head.toString))

        when(service.getAgentDetails(any(), any())(any()))
          .thenReturn(Future.successful(Some(testAgentDetails)))

        when(service.removeAgentDetails(any(), any())(any()))
          .thenReturn(Future.successful(false))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
