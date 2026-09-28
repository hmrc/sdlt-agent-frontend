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

import com.typesafe.config.Config
import uk.gov.hmrc.http.HttpReads.Implicits.*
import com.google.inject.ImplementedBy
import connectors.HttpResponseOps.*
import org.apache.pekko.actor.ActorSystem
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, Retries}
import models.*
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.{Configuration, Logging}
import play.api.libs.ws.JsonBodyWritables.*
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject
import java.net.URL

@ImplementedBy(classOf[RateLimitedAllowListConnectorImpl])
trait RateLimitedAllowListConnector {

  def checkAllowList(feature: String, charityReference: String)(using hc: HeaderCarrier): Future[Boolean]
}

class RateLimitedAllowListConnectorImpl @Inject() (
  http: HttpClientV2,
  config: Configuration,
  servicesConfig: ServicesConfig,
  val actorSystem: ActorSystem
)(using
  ExecutionContext
) extends RateLimitedAllowListConnector
    with Retries
    with Logging {

  val baseUrl: String       = servicesConfig.baseUrl("rate-limited-allow-list")
  def configuration: Config = config.underlying
  val serviceName: String   = configuration.getString("splitter.serviceName")

  private val checkAllowListUrl: String = s"$baseUrl/rate-limited-allow-list/services/$serviceName/features/"

  override def checkAllowList(feature: String, charityReference: String)(using hc: HeaderCarrier): Future[Boolean] = {
    val url: String = s"$checkAllowListUrl$feature"
    retryFor("checkAllowList") { case _ => true } {
      http
        .post(URL(url))
        .withBody(Json.toJson(AllowListCheckRequest(identifier = charityReference)))
        .execute[HttpResponse]
        .flatMap { response =>
          if response.status == 200
          then
            response
              .parseJSON[AllowListCheckResponse]()
              .fold(
                error => {
                  logger.error(s"Failed to parse response from POST $url: $error")
                  Future.failed(Exception(error))
                },
                response => Future.successful(response.included)
              )
          else Future.failed(Exception(s"Request to POST $url failed because of ${response.body}"))
        }
    }
  }
}
