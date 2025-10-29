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
import models.NormalMode
import models.responses.addresslookup.JourneyInitResponse.AddressLookupResponse
import models.responses.addresslookup.JourneyOutcomeResponse.AddressLookupJourneyOutcome
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logger

class AddressLookupConnector @Inject()(val appConfig: FrontendAppConfig,
                                        http: HttpClientV2,
                                        val messagesApi: MessagesApi)(implicit ec: ExecutionContext) {

  private val baseUrl: String = appConfig.addressLookupBaseUrl
  val addressLookupInitializeUrl : String = s"$baseUrl/api/v2/init"
  val addressLookupOutcomeUrl: String => String = (id: String) => s"$baseUrl/api/v2/confirmed?id=$id"

  private val sessionTimeout: Long = appConfig.sessionTimeOut
  private val addressLookupTimeoutUrl: String = appConfig.addressLookupTimeoutUrl

  private val langResourcePrefix : String = "manageAgents.addressLookup"

  private val continueUrl = appConfig.loginContinueUrl +
    controllers.manageAgents.routes.AddressLookupController.onSubmit(NormalMode).url

  private def setJourneyOptions(): Seq[(String, JsValue)] = {
    Seq(
      "continueUrl" -> JsString(continueUrl),
      "ukMode" -> JsBoolean(true),
      "selectPageConfig" -> JsObject(
        Seq(
          "proposalListLimit" -> JsNumber(30),
          "showSearchLinkAgain" -> JsBoolean(true)
        )
      ),
      "confirmPageConfig" -> JsObject(
        Seq(
          "showChangeLink" -> JsBoolean(false),
          "showSubHeadingAndInfo" -> JsBoolean(false),
          "showSearchAgainLink" -> JsBoolean(false),
          "showConfirmChangeText" -> JsBoolean(false),
        )
      ),
      "manualAddressEntryConfig" -> JsObject(
        Seq(
          "line1MaxLength" -> JsNumber(255),
          "line2MaxLength" -> JsNumber(255),
          "line3MaxLength" -> JsNumber(255),
          "townMaxLength" -> JsNumber(255)
        )
      ),
      "timeoutConfig" -> JsObject(
        Seq(
          "timeoutAmount" -> JsNumber(sessionTimeout),
          "timeoutUrl" -> JsString(addressLookupTimeoutUrl)
        )
      )
    )
  }

  private def setLabels(lang : Lang): Seq[(String, JsObject)] = {
    Seq(
      "selectPageLabels" -> JsObject(
        Seq(
          "heading" -> JsString(messagesApi.preferred( Seq( lang ) )(s"$langResourcePrefix.select.heading"))
        )
      ),
      "lookupPageLabels" -> JsObject(
        Seq(
          "heading" -> JsString(messagesApi.preferred( Seq( lang ) )(s"$langResourcePrefix.lookup.heading"))
        )
      ),
      "confirmPageLabels" -> JsObject(
        Seq(
          "heading" -> JsString(messagesApi.preferred( Seq( lang ) )(s"$langResourcePrefix.confirm.heading"))
        )
      ),
      "editPageLabels" -> JsObject(
        Seq(
          "heading" -> JsString(messagesApi.preferred( Seq( lang ) )(s"$langResourcePrefix.edit.heading"))
        )
      )
    )
  }

  private def buildConfig: JsValue = {
    JsObject(
      Seq(
        "version" -> JsNumber(2),
        "options" -> JsObject(
          setJourneyOptions()
        ),
        "labels" -> JsObject(
          Seq(
            "en" -> JsObject(
              setLabels( Lang("en") )
            ),
            "cy" -> JsObject(
              setLabels( Lang("cy"))
            )
          )
        )
      )
    )
  }

  // Step 1: Journey start/init
  def initJourney(storn: String)(implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
    import play.api.libs.ws.writeableOf_JsValue
    val payload: JsValue = buildConfig
    Logger("application").debug(s"[AddressLookupConnector] - body: ${Json.stringify(payload)}")
    http.post(url"$addressLookupInitializeUrl")
      .withBody(payload)
      .execute[AddressLookupResponse]
  }

  // Step 2: Extract journey result/outcome
  def getJourneyOutcome(id: String)
                       (implicit hc: HeaderCarrier): Future[AddressLookupJourneyOutcome] = {
    Logger("application").info(s"[AddressLookupConnector] - Extract address: ${addressLookupOutcomeUrl(id)}")
    http.get(url"${addressLookupOutcomeUrl(id)}").execute[AddressLookupJourneyOutcome]
  }

}