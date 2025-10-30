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
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import controllers.routes._

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with MockitoSugar {

  "Index Controller" - {

    "onPageLoad" - {
      s"must redirect to the Landing page with the userId added to the session" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.manageAgents.routes.AgentOverviewController.onPageLoad(1).url
          verify(mockSessionRepository).set(any[UserAnswers])
        }
      }

      "must update UserAnswers model with the userId pulled from the request" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(captor.capture())

          val savedUserAnswers = captor.getValue
          savedUserAnswers.id must not be empty

          verify(mockSessionRepository).set(argThat((ua: UserAnswers) =>
            ua.id.contains(savedUserAnswers.id)
          ))
        }
      }
    }
  }
}
