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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  private val appConfig = applicationBuilder().build().injector.instanceOf[FrontendAppConfig]

  private val testFrontendAppConfigWithTrafficSplitEnabled = new FrontendAppConfig(
    Configuration
      .from(
        Map(
          "splitter.trafficSplitEnabled" -> "true",
          "urls.legacySdltServiceUrl"    -> "/stamp-taxes"
        )
      )
      .withFallback(applicationBuilder().build().configuration)
  )

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository, appConfig, mock[RateLimitedAllowListConnector]) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = refine(request).map(_.toOption.get)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(None)
        val action = new Harness(sessionRepository)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", "STN001", AffinityGroup.Organisation)).futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
        val action = new Harness(sessionRepository)

        val result = action.callTransform(new IdentifierRequest(FakeRequest(), "id", "STN001", AffinityGroup.Organisation)).futureValue

        result.userAnswers mustBe defined
      }
    }

    "redirect to legacy sdlt service url when user is not on the allow list" in {
      val mockSessionRepository             = mock[SessionRepository]
      val mockRateLimitedAllowListConnector = mock[RateLimitedAllowListConnector]

      val action = new DataRetrievalActionImpl(
        mockSessionRepository,
        testFrontendAppConfigWithTrafficSplitEnabled,
        mockRateLimitedAllowListConnector
      )

      when(mockSessionRepository.get("id")) thenReturn Future(None)
      when(mockRateLimitedAllowListConnector.checkAllowList(eqTo("sdlt-filing-private-beta-2026"), eqTo("STN001"))(using any()))
        .thenReturn(Future.successful(false))

      val identifierRequest = IdentifierRequest(FakeRequest(), "id", "STN001", AffinityGroup.Organisation)

      val result = action.invokeBlock(
        identifierRequest,
        (_: OptionalDataRequest[?]) => Future.successful(Ok)
      )
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/stamp-taxes/org/STN001")
    }

    "redirect agent users to legacy sdlt service url when user is not on the allow list" in {
      val mockSessionRepository             = mock[SessionRepository]
      val mockRateLimitedAllowListConnector = mock[RateLimitedAllowListConnector]

      val action = new DataRetrievalActionImpl(
        mockSessionRepository,
        testFrontendAppConfigWithTrafficSplitEnabled,
        mockRateLimitedAllowListConnector
      )

      when(mockSessionRepository.get("id")) thenReturn Future(None)
      when(mockRateLimitedAllowListConnector.checkAllowList(any(), any())(using any()))
        .thenReturn(Future.successful(false))

      val result = action.invokeBlock(
        IdentifierRequest(FakeRequest(), "id", "STN001", AffinityGroup.Agent),
        (_: OptionalDataRequest[?]) => Future.successful(Ok)
      )
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/stamp-taxes/agent/STN001")
    }

    "refines IdentifierRequest into an OptionalDataRequest when session data doesn't exist and user is on the allow list" in {
      val mockSessionRepository             = mock[SessionRepository]
      val mockRateLimitedAllowListConnector = mock[RateLimitedAllowListConnector]

      val action = new DataRetrievalActionImpl(
        mockSessionRepository,
        testFrontendAppConfigWithTrafficSplitEnabled,
        mockRateLimitedAllowListConnector
      )

      when(mockSessionRepository.get("id")) thenReturn Future(None)
      when(mockRateLimitedAllowListConnector.checkAllowList(any(), any())(using any()))
        .thenReturn(Future.successful(true))

      val result = action.invokeBlock(
        IdentifierRequest(FakeRequest(), "id", "STN001", AffinityGroup.Organisation),
        (req: OptionalDataRequest[?]) =>
          req.userAnswers must not be defined
          Future.successful(Ok)
      )
      status(result) mustBe OK
    }

    "refines IdentifierRequest into an OptionalDataRequest without checking the allow list when session data exists" in {
      val mockSessionRepository             = mock[SessionRepository]
      val mockRateLimitedAllowListConnector = mock[RateLimitedAllowListConnector]

      val action = new DataRetrievalActionImpl(
        mockSessionRepository,
        testFrontendAppConfigWithTrafficSplitEnabled,
        mockRateLimitedAllowListConnector
      )

      when(mockSessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))

      val result = action.invokeBlock(
        IdentifierRequest(FakeRequest(), "id", "STN001", AffinityGroup.Organisation),
        (req: OptionalDataRequest[?]) =>
          req.userAnswers mustBe defined
          Future.successful(Ok)
      )
      status(result) mustBe OK
      verify(mockRateLimitedAllowListConnector, never()).checkAllowList(any(), any())(using any())
    }
  }
}
