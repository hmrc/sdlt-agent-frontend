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
import models.manageAgents.AgentContactDetails
import models.{NormalMode, Mode, CheckMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.manageAgents.{AgentContactDetailsPage, AgentNamePage, ConfirmAgentContactDetailsPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.StampDutyLandTaxService
import views.html.manageAgents.ConfirmAgentContactDetailsView

import scala.concurrent.Future

class ConfirmAgentContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {
    val agentName = "John Doe"

    val formProvider = new ConfirmAgentContactDetailsFormProvider()
    val form: Form[Boolean] = formProvider()

    val agentContactDetails: AgentContactDetails = AgentContactDetails(Some("phone"), Some("email"))

    val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value)).build()

    def noOnwardRoute: Call = Call("GET", "/stamp-duty-land-tax-agent/manage-agents/check-answers")

    val userAnswersWithAgentContactDetails: UserAnswers = Some(emptyUserAnswers
      .set(AgentNamePage, agentName).success.value
      .set(ConfirmAgentContactDetailsPage, true).success.value
      .set(AgentContactDetailsPage, agentContactDetails).success.value).value

    val userAnswersWithOutAgentContactDetails: UserAnswers = userAnswersWithAgentContactDetails.remove(AgentContactDetailsPage).success.value

    def yesOnwardRoute: Call = Call("GET", "/stamp-duty-land-tax-agent/manage-agents/enter-contact-details")
    def yesOnwardRouteCheckMode: Call = Call("GET", "/stamp-duty-land-tax-agent/manage-agents/change-enter-contact-details")

    val journeyRecoveryRoute: String = controllers.routes.JourneyRecoveryController.onPageLoad().url

    val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(FakeRequest())

    lazy val confirmAgentContactDetailsRoute: String = controllers.manageAgents.routes.ConfirmAgentContactDetailsController.onPageLoad(NormalMode).url
    def confirmAgentContactDetailsOnSubmitRoute(mode: Mode): String = controllers.manageAgents.routes.ConfirmAgentContactDetailsController.onSubmit(mode).url
    

  }

  "ConfirmAgentContactDetailsController" - {

    "must return OK and the correct view for a GET" in new Fixture {

      running(application) {
        val request = FakeRequest(GET, confirmAgentContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmAgentContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, agentName, NormalMode)(request, messages).toString
      }
    }
    
    "must auto fill form correctly when the question has been answered previously" in new Fixture {
      override val application: Application = applicationBuilder(userAnswers = Some(userAnswersWithAgentContactDetails)).build()
      
      running(application) {
        val request = FakeRequest(GET, confirmAgentContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmAgentContactDetailsView]
        
        val agentName = userAnswersWithAgentContactDetails.get(AgentNamePage).get
        
        val form = formProvider()
        
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), agentName, NormalMode)(request, messages).toString
      }
    }

    "must redirect to the Journey Recovery page for a GET when agent details are not found" in  new Fixture {

      override val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmAgentContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    
    "must return BadRequest and errors when invalid data is submitted " in new Fixture {
      val mockSessionRepo: SessionRepository = mock[SessionRepository]
      when(mockSessionRepo.set(any())) thenReturn Future.successful(true)

      override val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepo))
        .build()

      running(application) {

        val request = FakeRequest(POST, confirmAgentContactDetailsOnSubmitRoute(NormalMode))
          .withFormUrlEncodedBody(("value", "malformedBodyRequest"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
    
    "must redirect to the agent contact details page when valid YES option is submitted in NormalMode" in new Fixture {
      val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

      when(mockService.getAgentName(any())).thenReturn(Right(agentName))
      override val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value))
        .build()

      running(application) {

        val request = FakeRequest(POST, confirmAgentContactDetailsOnSubmitRoute(NormalMode))
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual yesOnwardRoute.url
      }
    }
    
    "must redirect to the agent contact details page when valid YES option is submitted in CheckMode" in new Fixture {
      val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

      when(mockService.getAgentName(any())).thenReturn(Right(agentName))
      override val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value))
        .build()

      running(application) {

        val request = FakeRequest(POST, confirmAgentContactDetailsOnSubmitRoute(CheckMode))
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual yesOnwardRouteCheckMode.url
      }
    }

    "must remove AgentContactDetailsPage and redirect to CheckYourAnswersController when selecting No is submitted in CheckMode" in new Fixture {
      val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

      when(mockService.getAgentName(any())).thenReturn(Right(agentName))

      override val application: Application = applicationBuilder(Some(userAnswersWithAgentContactDetails))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()

      running(application) {

        val request = FakeRequest(POST, confirmAgentContactDetailsOnSubmitRoute(CheckMode))
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual noOnwardRoute.url

      }
    }

    "must redirect to CheckYourAnswersController when user selects NO option and is submitted in NormalMode" in new Fixture {

        val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]

        when(mockService.getAgentName(any())).thenReturn(Right(agentName))

        override val application: Application = applicationBuilder(
          userAnswers = Some(
            emptyUserAnswers
              .set(AgentNamePage, agentName).success.value
          )
        )
          .build()

        running(application) {

          val request = FakeRequest(POST, confirmAgentContactDetailsOnSubmitRoute(NormalMode))
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual noOnwardRoute.url
        }
      }
    
    "must redirect to journey recovery when fail to extract agentName" in new Fixture {
      val mockService: StampDutyLandTaxService = mock[StampDutyLandTaxService]
      when(mockService.getAgentName(any())) thenReturn Left(Error("Failed to extract agentName"))

      val mockSessionRepo: SessionRepository = mock[SessionRepository]
      when(mockSessionRepo.set(any())) thenReturn Future.successful(true)

      override val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AgentNamePage, agentName).success.value))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepo))
        .overrides(bind[StampDutyLandTaxService].toInstance(mockService))
        .build()
      running(application) {

        val request = FakeRequest(POST, confirmAgentContactDetailsOnSubmitRoute(NormalMode))
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual journeyRecoveryRoute
      }
    }
    
  }

}
