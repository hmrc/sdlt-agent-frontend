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
import models.responses.addresslookup.JourneyInitResponse.{JourneyInitFailureResponse, JourneyInitSuccessResponse}
import models.responses.addresslookup.JourneyOutcomeResponse.UnexpectedGetStatusFailure
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AddressLookupService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET

import scala.concurrent.Future

class AddressLookupControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {
    val service: AddressLookupService = mock[AddressLookupService]
    val app: Application =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AddressLookupService].toInstance(service))
        .build()

    val addressLookupInit: String = controllers.manageAgents.routes.AddressLookupController.onPageLoad(NormalMode).url

    val id: String = "idToExtractAddress"
    val addressLookupExtract: Mode => String = (mode: Mode) => controllers.manageAgents.routes.AddressLookupController.onSubmit(mode).url + s"?id=$id"
    val thereIsProblemUrl : String = controllers.routes.JourneyRecoveryController.onPageLoad(None).url()

    val userId: String = "userID"

    val userAnswers: UserAnswers = UserAnswers(id = userId)
  }

  "AddressLookupController init AL journey" - {

    "return location on success" in new Fixture {
      when(service.initJourney(any(), any(), any())(any[HeaderCarrier], any[Messages]))
        .thenReturn(Future.successful(Right(JourneyInitSuccessResponse(Some("locationUrl")))))

      running(app) {
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, addressLookupInit)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "locationUrl"
      }
    }

    "redirect to default error page: Journey Location is None" in new Fixture {

      when(service.initJourney(any(), any(), any())(any[HeaderCarrier], any[Messages]))
        .thenReturn(Future.successful(Right(JourneyInitSuccessResponse(None))))

      running(app) {
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, addressLookupInit)

        val result = route(app, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual thereIsProblemUrl
      }

    }

    "return failure status on error" in new Fixture {

      when(service.initJourney(any(), any(), any())(any[HeaderCarrier], any[Messages]))
        .thenReturn(Future.successful(Left(JourneyInitFailureResponse(INTERNAL_SERVER_ERROR))))

      running(app) {
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, addressLookupInit)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual thereIsProblemUrl
      }

    }
  }

  "AddressLookupController get AL journey result" - {

    "return AddressDetails on success: NormalMode" in new Fixture {
      when(service.getJourneyOutcome(any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(userAnswers)))

      running(app) {
        val request =
          FakeRequest(GET, addressLookupExtract(NormalMode))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/stamp-duty-land-tax-agent/agent-details/agent-contact-details"

        verify(service, times(1)).getJourneyOutcome(any(), any())(any[HeaderCarrier])
      }
    }

    "return AddressDetails on success: CheckMode" in new Fixture {
      when(service.getJourneyOutcome(any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(userAnswers)))

      running(app) {
        val request =
          FakeRequest(GET, addressLookupExtract(CheckMode))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/stamp-duty-land-tax-agent/agent-details/check-your-answers"

        verify(service, times(1)).getJourneyOutcome(any(), any())(any[HeaderCarrier])
      }
    }

    "return failure on error" in new Fixture {
      when(service.getJourneyOutcome(any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(new Error("Some Error")) ))

      running(app) {
        val request =
          FakeRequest(GET, addressLookupExtract(NormalMode) )

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/stamp-duty-land-tax-agent/there-is-a-problem"
        verify(service, times(1)).getJourneyOutcome(any(), any())(any[HeaderCarrier])
      }
    }
  }
}