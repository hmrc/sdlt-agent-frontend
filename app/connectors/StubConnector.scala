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

import config.FrontendAppConfig
import models.AgentDetails
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.Request
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StubConnector @Inject()(val http: HttpClientV2,
                              val config: FrontendAppConfig)
                             (implicit ec: ExecutionContext) {

  lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  lazy val sdltStubUrl: String = config.baseUrl("stamp-duty-land-tax-stub")

  def stubManageAgentQuestions(detailsId: String)(implicit hc: HeaderCarrier,
                                                  request: Request[_]): Future[AgentDetails] = {
    // TODO: Implement stub response
    http.get(url"$sdltStubUrl/stamp-duty-land-tax-stub/manage-agent/details?detailsId=$detailsId")
      .execute[AgentDetails]
      .recover{
        case e => throw logResponse(e, "stubPrelimQuestions")
      }
  }

  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}
