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
import models.{AgentDetailsResponse, UserAnswers}
import pages.manageAgents.StornPage
import play.api.libs.json.Json
import services.StampDutyLandTaxService
import utils.mangeAgents.AgentDetailsTestUtil
import viewmodels.govuk.SummaryListFluency
import viewmodels.manageAgents.checkAnswers.{AddressSummary, AgentNameSummary, ContactEmailSummary, ContactPhoneNumberSummary}
import views.html.manageAgents.CheckYourAnswersView
import base.SpecBase
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

  "Check Your Answers Controller (with no ARN population)" - {

    "must return OK and the correct view for a GET" in {

      val testData = Json.obj(
        "agentName" -> "John",
        "agentAddress" -> "123 Road",
        "agentContactDetails" -> Json.obj(
          "contactTelephoneNumber" -> "07123456789",
          "contactEmail" -> "john@example.com"
        ),
        "agentAddress" -> Json.obj(
          "auditRef" -> "d8819c6a-8d78-4219-8f9d-40b119edcb3d",
          "address" -> Json.obj(
            "lines" -> Json.arr(
              "10 Downing Street",
              "South Kensington",
              "London",
              "SW7 5JT"
            )
          )
        )
      )

      val ua = UserAnswers("id", testData).set(StornPage, testStorn).success.value

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
                AgentNameSummary.row(ua)(messages(application)).get,
                AddressSummary.row(ua)(messages(application)).get,
                ContactPhoneNumberSummary.row(ua)(messages(application)).get,
                ContactEmailSummary.row(ua)(messages(application)).get
              )),
            postAction = controllers.manageAgents.routes.SubmitAgentController.onSubmit()
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

      val testAgentResponse = AgentDetailsResponse(
        agentReferenceNumber = testArn,
        agentName = "Harborview Estates",
        houseNumber = "42",
        addressLine1 = "Queensway",
        addressLine2 = None,
        addressLine3 = "Birmingham",
        addressLine4 = None,
        postcode = Some("B2 4ND"),
        phone = Some("01214567890"),
        email = "info@harborviewestates.co.uk"
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
        body must include(s"""action="${controllers.manageAgents.routes.SubmitAgentController.onSubmit().url}"""")
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
