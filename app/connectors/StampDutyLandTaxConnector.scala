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
import models.responses.organisation.SdltOrganisationResponse
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
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
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/submit"

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

  def submitAgentDetails(agentDetailsBeforeCreation: AgentDetailsBeforeCreation)
                        (implicit hc: HeaderCarrier): Future[SubmitAgentDetailsResponse] =
    http
      .post(submitAgentDetailsUrl)
      .withBody(Json.toJson(agentDetailsBeforeCreation))
      .execute[SubmitAgentDetailsResponse]
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

  def removeAgentDetails(storn: String, agentReferenceNumber: String)
                        (implicit hc: HeaderCarrier): Future[Unit] =
    http
      .get(removeAgentDetailsUrl(storn, agentReferenceNumber))
      .execute[HttpResponse]
      .flatMap { response =>
        if(response.status == 200) Future.unit
        else Future.failed(new RuntimeException(s"Failed to remove agent: status=${response.status}, body=${response.body}"))
      }
      .recover {
        case e: Throwable =>
          logger.error(s"[StampDutyLandTaxConnector][removeAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
}
