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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")


  protected lazy val rootServices = "microservice.services"

  protected lazy val defaultProtocol: String =
    configuration
      .getOptional[String](s"$rootServices.protocol")
      .getOrElse("http")

  def getConfString(confKey: String, defString: => String): String =
    configuration
      .getOptional[String](s"$rootServices.$confKey")
      .getOrElse(defString)

  def getConfInt(confKey: String, defInt: => Int): Int =
    configuration
      .getOptional[Int](s"$rootServices.$confKey")
      .getOrElse(defInt)

  protected def config(serviceName: String): Configuration =
    configuration
      .getOptional[Configuration](s"$rootServices.$serviceName")
      .getOrElse(throw new IllegalArgumentException(s"Configuration for service $serviceName not found"))

  def baseUrl(serviceName: String): String = {
    val protocol = getConfString(s"$serviceName.protocol", defaultProtocol)
    val host = getConfString(s"$serviceName.host", throwConfigNotFoundError(s"$serviceName.host"))
    val port = getConfInt(s"$serviceName.port", throwConfigNotFoundError(s"$serviceName.port"))
    s"$protocol://$host:$port"
  }

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "sdlt-agent-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/sdlt-agent-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  private def throwConfigNotFoundError(key: String) =
    throw new RuntimeException(s"Could not find config key '$key'")
}
