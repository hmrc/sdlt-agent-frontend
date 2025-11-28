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

import controllers.routes
import models.{NormalMode, UserAnswers}
import pages.manageAgents.{AgentOverviewPage, StornPage}
import play.api.libs.json.Json
import services.StampDutyLandTaxService
import utils.mangeAgents.AgentDetailsTestUtil
import viewmodels.govuk.SummaryListFluency
import viewmodels.manageAgents.checkAnswers.{AddressSummary, AgentNameSummary, ContactEmailSummary, ContactPhoneNumberSummary}
import views.html.manageAgents.CheckYourAnswersView
import base.SpecBase
import models.responses.CreatePredefinedAgentResponse
import models.responses.organisation.CreatedAgent
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with AgentDetailsTestUtil with MockitoSugar {

  private lazy val checkYourAnswersUrl: Option[String] => String = agentReferenceNumber =>
    controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad(agentReferenceNumber).url
  
  private lazy val submitAnswerUrl: Option[String] => String = agentReferenceNumber =>
    controllers.manageAgents.routes.CheckYourAnswersController.onSubmit(agentReferenceNumber).url
    
  "onPageLoad"- {
    "Check Your Answers Controller (with no ARN population)" - {

      "must return OK and the correct view for a GET" in {

        val ua = UserAnswers("id", testUserAnswers).set(StornPage, testStorn).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersUrl(None))

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              list = SummaryListViewModel(
                Seq(
                  AgentNameSummary.row(ua)(messages(application)),
                  AddressSummary.row(ua)(messages(application)),
                  ContactPhoneNumberSummary.row(ua)(messages(application)),
                  ContactEmailSummary.row(ua)(messages(application))
                ).flatten
              ),
              postAction = controllers.manageAgents.routes.CheckYourAnswersController.onSubmit(None)
            )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersUrl(None))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "Check Your Answers Controller (with ARN population)" - {

      "must call BE and redirect to Journey Recovery when agent is not found (None returned)" in {

        val service = mock[StampDutyLandTaxService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithStorn))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        when(service.getAgentDetails(any(), any())(any()))
          .thenReturn(Future.successful(None))

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersUrl(Some(testArn)))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          verify(service, times(1)).getAgentDetails(any(), any())(any())
        }
      }

      "must redirect to Journey Recovery when BE call fails unexpectedly" in {

        val service = mock[StampDutyLandTaxService]

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithStorn))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        when(service.getAgentDetails(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersUrl(Some(testArn)))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return OK and render from freshly populated UA when BE returns details" in {

        val service = mock[StampDutyLandTaxService]

        val testAgentResponse: CreatedAgent = CreatedAgent(
          storn = testStorn,
          agentId = None,
          name = "Harborview Estates",
          houseNumber = None,
          address1 = "42 Queensway",
          address2 = None,
          address3 = Some("Birmingham"),
          address4 = None,
          postcode = Some("B2 4ND"),
          phone = "01214567890",
          email = "info@harborviewestates.co.uk",
          dxAddress = None,
          agentResourceReference = testArn
        )


        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithStorn))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        when(service.getAgentDetails(any(), any())(any()))
          .thenReturn(Future.successful(Some(testAgentResponse)))

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersUrl(Some(testArn)))
          val result = route(application, request).value
          val body = contentAsString(result)

          status(result) mustEqual OK

          body must include("Harborview Estates")
          body must include("Queensway")
          body must include("Birmingham")
          body must include("B2 4ND")
          body must include("01214567890")
          body must include("info@harborviewestates.co.uk")
          body must include(s"""action="${controllers.manageAgents.routes.CheckYourAnswersController.onSubmit(Some(testArn)).url}"""")
        }
      }

      "must return OK (no BE call) when ARN not provided (None) and render from existing UA" in {

        val service = mock[StampDutyLandTaxService]

        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersUrl(None))
          val result = route(application, request).value
          val body = contentAsString(result)

          status(result) mustEqual OK

          verify(service, times(0)).getAgentDetails(any(), any())(any())

          body must include("John")
        }
      }
    }
  }
  "onSubmit()" - {
    "with no ARN population" - {
      "must redirect to Journey Recovery Controller when agentName is missing from userAnswers " in {

        val service = mock[StampDutyLandTaxService]
        val application = applicationBuilder(userAnswers = Some(populatedUserAnswersWithoutAgentName))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        when(service.submitAgentDetails(any())(any()))
          .thenReturn(Future.successful(None))

        running(application) {
          val request = FakeRequest(POST, submitAnswerUrl(None))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          verify(service, times(0)).submitAgentDetails(any())(any())


        }
      }
      "must redirect to AgentOverviewPage with flash after successfully submitting agentDetails to StampDutyLandTaxService.submitAgentDetails in BE" in {
        val navigator = new Navigator
        val service = mock[StampDutyLandTaxService]
        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        when(service.submitAgentDetails(any())(any()))
          .thenReturn(Future.successful(CreatePredefinedAgentResponse("agentResourceRef", "agentId")))

        running(application) {
          val request = FakeRequest(POST, submitAnswerUrl(None))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual navigator.nextPage(AgentOverviewPage, NormalMode, emptyUserAnswers).url

          flash(result).get("agentCreated") mustBe Some("John")

          verify(service, times(1)).submitAgentDetails(any())(any())

        }
      }
      "must throw and exception after getting error response from StampDutyLandTaxService.submitAgentDetails" in {
        val service = mock[StampDutyLandTaxService]
        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers))
          .overrides(bind[StampDutyLandTaxService].toInstance(service))
          .build()

        when(service.submitAgentDetails(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("An unexpected error occurred")))

        running(application) {
          val request = FakeRequest(POST, submitAnswerUrl(None))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

          verify(service, times(1)).submitAgentDetails(any())(any())

        }

      }
      "with ARN population" - {
        "must redirect to Journey Recovery controller when ARN is not found " in {
          val service = mock[StampDutyLandTaxService]

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithStorn))
            .overrides(bind[StampDutyLandTaxService].toInstance(service))
            .build()

          running(application) {
            val request = FakeRequest(POST, submitAnswerUrl(Some(testArn)))
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          }
        }
      }


    }
  }

}
