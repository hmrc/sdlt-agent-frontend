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

package controllers.manageAgent

import base.SpecBase
import models.{NormalMode, UserAnswers}
import models.responses.addresslookup.JourneyInitResponse.{JourneyInitFailureResponse, JourneyInitSuccessResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import services.AddressLookupService
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET
import play.api.test.Helpers.*

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
    val addressLookupExtract: String = controllers.manageAgents.routes.AddressLookupController.onSubmit(NormalMode).url + s"?id=$id"

    val userId : String = "userID"

    val userAnswers: UserAnswers = UserAnswers(id = userId)
  }

  "AddressLookupController init AL journey" - {

    "return location on success" in new Fixture {
      when(service.initJourney(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(JourneyInitSuccessResponse(Some("locationUrl")))))

      running(app) {
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, addressLookupInit)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "locationUrl"
      }

    }

    "return failure status on error" in new Fixture {

      when(service.initJourney(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(JourneyInitFailureResponse(INTERNAL_SERVER_ERROR) )) )

      running(app) {
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, addressLookupInit)

        val result = route(app, request).value

        // TODO: we should have redirect to the default Journey Error Page
        //status(result) mustEqual INTERNAL_SERVER_ERROR
        //redirectLocation(result).value mustEqual "locationUrl"
      }

    }

  }

  "AddressLookupController get AL journey result" - {

    "return AddressDetails on success" in new Fixture {
      when( service.getJourneyOutcome( any(), any() )(any[HeaderCarrier]) )
        .thenReturn(Future.successful( Right(userAnswers) ))

      running(app) {
        val request =
          FakeRequest(POST, addressLookupExtract)
            //.withFormUrlEncodedBody( ("id", id) )

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        // TODO: actual redirect location TBC
        redirectLocation(result).value mustEqual "/stamp-duty-land-tax-agent"
      }
    }

    "return AddressDetails on error" in new Fixture {
      when(service.getJourneyOutcome(any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(userAnswers)))

      running(app) {
        val request =
          FakeRequest(POST, addressLookupExtract)
        //.withFormUrlEncodedBody( ("id", id) )

        // TODO: we should have redirect to the default Journey Error Page
        route(app, request).value
      }

    }
  }
}