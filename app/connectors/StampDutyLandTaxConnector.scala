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

package connectors

import models.requests.CreatePredefinedAgentRequest
import models.requests.DeletePredefinedAgentRequest
import models.requests.UpdatePredefinedAgent
import models.responses.CreatePredefinedAgentResponse
import models.responses.DeletePredefinedAgentResponse
import models.responses.UpdatePredefinedAgentResponse
import models.responses.organisation.SdltOrganisationResponse
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.LoggerUtil.logError

import java.net.URL
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class StampDutyLandTaxConnector @Inject() (
    http: HttpClientV2,
    config: ServicesConfig
)(implicit ec: ExecutionContext) {

  private val base = config.baseUrl("stamp-duty-land-tax")

  private val getSdltOrganisationUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/get-sdlt-organisation?storn=$storn"

  private val submitAgentDetailsUrl: URL =
    url"$base/stamp-duty-land-tax/create/predefined-agent"

  private val deletePredefinedAgentUrl: URL =
    url"$base/stamp-duty-land-tax/manage-agents/delete/predefined-agent"
  private val updateAgentDetailsUrl: URL =
    url"$base/stamp-duty-land-tax/manage-agents/update/predefined-agent"

  def getSdltOrganisation(
      storn: String
  )(implicit hc: HeaderCarrier): Future[SdltOrganisationResponse] =
    http
      .get(getSdltOrganisationUrl(storn))
      .execute[SdltOrganisationResponse]
      .recover { case e: Throwable =>
        logError(
          s"[StampDutyLandTaxConnector][getSdltOrganisation]: ${e.getMessage}"
        )
        throw new RuntimeException(e.getMessage)
      }

  def submitAgentDetails(
      createPredefinedAgentRequest: CreatePredefinedAgentRequest
  )(implicit hc: HeaderCarrier): Future[CreatePredefinedAgentResponse] =
    http
      .post(submitAgentDetailsUrl)
      .withBody(Json.toJson(createPredefinedAgentRequest))
      .execute[CreatePredefinedAgentResponse]
      .recover { case e: Throwable =>
        logError(
          s"[StampDutyLandTaxConnector][submitAgentDetails]: ${e.getMessage}"
        )
        throw new RuntimeException(e.getMessage)
      }

  def updateAgentDetails(
      UpdatePredefinedAgent: UpdatePredefinedAgent
  )(implicit hc: HeaderCarrier): Future[UpdatePredefinedAgentResponse] =
    http
      .post(updateAgentDetailsUrl)
      .withBody(Json.toJson(UpdatePredefinedAgent))
      .execute[Either[UpstreamErrorResponse, UpdatePredefinedAgentResponse]]
      .flatMap {
        case Right(response) =>
          Future.successful(response)
        case Left(error) =>
          Future.failed(error)
      }
      .recover { case e: Throwable =>
        logError(
          s"[StampDutyLandTaxConnector][updatePredefinedAgent]: ${e.getMessage}"
        )
        throw new RuntimeException(e.getMessage)
      }

  def deletePredefinedAgent(
      deletePredefinedAgentRequest: DeletePredefinedAgentRequest
  )(implicit hc: HeaderCarrier): Future[DeletePredefinedAgentResponse] =
    http
      .post(deletePredefinedAgentUrl)
      .withBody(Json.toJson(deletePredefinedAgentRequest))
      .execute[Either[UpstreamErrorResponse, DeletePredefinedAgentResponse]]
      .flatMap {
        case Right(response) =>
          Future.successful(response)
        case Left(error) =>
          Future.failed(error)
      }
      .recover { case e: Throwable =>
        logError(
          s"[StampDutyLandTaxConnector][deletePredefinedAgent]: ${e.getMessage}"
        )
        throw new RuntimeException(e.getMessage)
      }
}
