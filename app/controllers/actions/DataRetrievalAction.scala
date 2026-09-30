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

import config.FrontendAppConfig
import connectors.RateLimitedAllowListConnector
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(
                                         val sessionRepository: SessionRepository,
                                         config: FrontendAppConfig,
                                         rateLimitedAllowListConnector: RateLimitedAllowListConnector
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.request, request.request.session)

    sessionRepository.get(request.userId).flatMap {
      case None =>
        checkAllowList(config.useRateLimitedAllowList, config.splitterAllowListName, request.storn)
          .map {
            case false =>
              Left(Results.Redirect(config.legacySdltServiceUrl(request)))

            case true =>
              Right(OptionalDataRequest(request.request, request.userId, request.storn, None))
          }
      case userAnswers =>
        Future.successful(Right(OptionalDataRequest(request.request, request.userId, request.storn, userAnswers)))
    }
  }

  private def checkAllowList(useRateLimitedAllowList: Boolean, allowListName: String, storn: String)(
    implicit hc: HeaderCarrier
  ): Future[Boolean] =
    if (useRateLimitedAllowList) {
      rateLimitedAllowListConnector.checkAllowList(allowListName, storn)
    } else {
      Future.successful(true)
    }
}

trait DataRetrievalAction extends ActionRefiner[IdentifierRequest, OptionalDataRequest]
