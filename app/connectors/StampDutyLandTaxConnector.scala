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

import models.AgentDetails
import models.responses.SubmitAgentDetailsResponse
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StampDutyLandTaxConnector @Inject()(http: HttpClientV2,
                                          config: ServicesConfig)
                                         (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("stamp-duty-land-tax")
  
  private val getAgentDetailsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/storn/$storn"

  private val getAllAgentDetailsUrl: String => URL = storn =>
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/get-all-agents/storn/$storn"

  private val submitAgentDetailsUrl: URL =
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/submit"

  private val removeAgentDetailsUrl: (String, String) => URL = (storn, agentRef) =>
    url"$base/stamp-duty-land-tax/manage-agents/agent-details/remove?storn=$storn&agentReferenceNumber=$agentRef"
  
  def getAgentDetails(storn: String)
                     (implicit hc: HeaderCarrier): Future[Option[AgentDetails]] =
    http
      .get(getAgentDetailsUrl(storn))
      .execute[Option[AgentDetails]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def getAllAgentDetails(storn: String)
                        (implicit hc: HeaderCarrier): Future[List[AgentDetails]] =
    http
      .get(getAllAgentDetailsUrl(storn))
      .execute[List[AgentDetails]]
      .recover {
        case e: Throwable =>
          logger.error(s"[getAllAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def submitAgentDetails(agentDetails: AgentDetails)
                        (implicit hc: HeaderCarrier): Future[SubmitAgentDetailsResponse] =
    http
      .post(submitAgentDetailsUrl)
      .withBody(Json.toJson(agentDetails))
      .execute[SubmitAgentDetailsResponse]
      .recover {
        case e: Throwable =>
          logger.error(s"[submitAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }

  def removeAgentDetails(storn: String, agentReferenceNumber: String)
                        (implicit hc: HeaderCarrier): Future[Boolean] =
    http
      .get(removeAgentDetailsUrl(storn, agentReferenceNumber))
      .execute[Boolean]
      .recover {
        case e: Throwable =>
          logger.error(s"[removeAgentDetails]: ${e.getMessage}")
          throw new RuntimeException(e.getMessage)
      }
}
