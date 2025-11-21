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

import models.{AgentDetailsAfterCreation, AgentDetailsRequest}
import models.responses.SubmitAgentDetailsResponse
import models.requests.CreatePredefinedAgentRequest
import models.responses.CreatePredefinedAgentResponse
import models.requests.DeletePredefinedAgentRequest
import models.responses.DeletePredefinedAgentResponse
import models.responses.organisation.SdltOrganisationResponse
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnector @Inject()(http: HttpClientV2,
                                          config: ServicesConfig)
                                         (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("stamp-duty-land-tax")

  private val getSdltOrganisationUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/get-sdlt-organisation?storn=$storn"

  private val submitAgentDetailsUrl: URL =
    url"$base/stamp-duty-land-tax/create/predefined-agent"

  private val deletePredefinedAgentUrl: URL =
    url"$base/stamp-duty-land-tax/manage-agents/delete/predefined-agent"
  private val updateAgentDetailsUrl: URL =
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/update"

  private val removeAgentDetailsUrl: (String, String) => URL = (storn, agentRef) =>
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/remove?storn=$storn&agentReferenceNumber=$agentRef"

  def getSdltOrganisation(storn: String)
                         (implicit hc: HeaderCarrier): Future[SdltOrganisationResponse] =
    http
      .get(getSdltOrganisationUrl(storn))
      .execute[SdltOrganisationResponse]
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][getSdltOrganisation]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def submitAgentDetails(createPredefinedAgentRequest: CreatePredefinedAgentRequest)
                        (implicit hc: HeaderCarrier): Future[CreatePredefinedAgentResponse] =
    http
      .post(submitAgentDetailsUrl)
      .withBody(Json.toJson(createPredefinedAgentRequest))
      .execute[CreatePredefinedAgentResponse]
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][submitAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def updateAgentDetails(agentDetailsAfterCreation: AgentDetailsAfterCreation)
                        (implicit hc: HeaderCarrier): Future[Unit] =
    http
      .put(updateAgentDetailsUrl)
      .withBody(Json.toJson(agentDetailsAfterCreation))
      .execute[Unit]
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][updateAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def deletePredefinedAgent(deletePredefinedAgentRequest: DeletePredefinedAgentRequest)
                        (implicit hc: HeaderCarrier): Future[DeletePredefinedAgentResponse] =
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
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][deletePredefinedAgent]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
}
