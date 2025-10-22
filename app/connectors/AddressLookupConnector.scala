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
import models.responses.addresslookup.JourneyInitResponse.AddressLookupResponse
import models.responses.addresslookup.JourneyOutcomeResponse.AddressLookupJourneyOutcome
import play.api.i18n.MessagesApi
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
  private val addressLookupInitializeUrl : String = s"$baseUrl/api/v2/init"
  private val addressLookupOutcomeUrl = (id: String) => s"$baseUrl/api/v2/confirmed?id=$id"

  //TODO: slice into smaller functions + sync actual config with Team-One|TL|Scott
  private def getAddressJson(): JsValue = {
    JsObject(
      Seq(
        "version" -> JsNumber(2),
        "options" -> JsObject(
          Seq(
            "continueUrl" -> JsString("..."),
            "homeNavHref" -> JsString("..."),
            "signOutHref" -> JsString("..."),

            "accessibilityFooterUrl" -> JsString("..."),
            "phaseFeedbackLink" -> JsString("/help/alpha"),
            "deskProServiceName" -> JsString("..."),
            "showPhaseBanner" -> JsBoolean(false),
            "alphaPhase" -> JsBoolean(false),
            "disableTranslations" -> JsBoolean(true),
            "showBackButtons" -> JsBoolean(false),
            "includeHMRCBranding" -> JsBoolean(true),

            "allowedCountryCodes" -> JsArray(Seq(JsString("GB"))),

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
                "timeoutAmount" -> JsNumber(900),
                "timeoutUrl" -> JsString("/timeout-uri"),
                "timeoutKeepAliveUrl" -> JsString("/keep-alive-uri")
              )
            ),

            "pageHeadingStyle" -> JsString("govuk-heading-xl")

          )
        ),
        "labels" -> JsObject(
          Seq(
            "en" -> JsObject(
              Seq(
                "appLevelLabels" -> JsObject(
                  Seq(
                    "navTitle" -> JsString(""),
                    "phaseBannerHtml" -> JsString("")
                  )
                ),
                "countryPickerLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Custom title"),
                    "heading" -> JsString("Custom heading"),
                    "countryLabel" -> JsString("Custom country label"),
                    "submitLabel" -> JsString("Custom submit label")
                  )
                ),
                "selectPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Choose address"),
                    "heading" -> JsString("Choose address"),
                    "headingWithPostcode" -> JsString("foo"),
                    "proposalListLabel" -> JsString("Please select one of the following addresses"),
                    "submitLabel" -> JsString("Continue"),
                    "searchAgainLinkText" -> JsString("Search again"),
                    "editAddressLinkText" -> JsString("Enter address manually")
                  )
                ),
                "lookupPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Find address"),
                    "heading" -> JsString("Find address"),
                    "afterHeadingText" -> JsString("We will use this address to send letters"),
                    "filterLabel" -> JsString("Property name or number (optional)"),
                    "postcodeLabel" -> JsString("Postcode"),
                    "submitLabel" -> JsString("Find address"),
                    "noResultsFoundMessage" -> JsString("Sorry, we couldn't find anything for that postcode."),
                    "resultLimitExceededMessage" -> JsString("There were too many results. Please add additional details to limit the number of results."),
                    "manualAddressLinkText" -> JsString("Enter the address manually")
                  )
                ),
                "confirmPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Confirm address"),
                    "heading" -> JsString("Review and confirm"),
                    "infoSubheading" -> JsString("Your selected address"),
                    "infoMessage" -> JsString("This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."),
                    "submitLabel" -> JsString("Confirm Address"),
                    "searchAgainLinkText" -> JsString("Search again"),
                    "changeLinkText" -> JsString("Edit address"),
                    "confirmChangeText" -> JsString("By confirming this change, you agree that the information you have given is complete and correct.")
                  )
                ),
                "editPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Enter address"),
                    "heading" -> JsString("Enter address"),
                    "organisationLabel" -> JsString("Organisation (optional)"),
                    "line1Label" -> JsString("Address line 1"),
                    "line2Label" -> JsString("Address line 2 (optional)"),
                    "line3Label" -> JsString("Address line 3 (optional)"),
                    "townLabel" -> JsString("Town/City"),
                    "postcodeLabel" -> JsString("Postcode (optional)"),
                    "countryLabel" -> JsString("Country"),
                    "submitLabel" -> JsString("Continue")
                  )
                )
              )
            ),
            "cy" -> JsObject(
              Seq(
                "appLevelLabels" -> JsObject(
                  Seq(
                    "navTitle" -> JsString(""),
                    "phaseBannerHtml" -> JsString("")
                  )
                ),
                "countryPickerLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Custom title - Welsh"),
                    "heading" -> JsString("Custom heading - Welsh"),
                    "countryLabel" -> JsString("Custom country label - Welsh"),
                    "submitLabel" -> JsString("Custom submit label - Welsh")
                  )
                ),
                "selectPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Choose address Welsh"),
                    "heading" -> JsString("Choose address Welsh"),
                    "headingWithPostcode" -> JsString("foo"),
                    "proposalListLabel" -> JsString("Please select one of the following addresses Welsh"),
                    "submitLabel" -> JsString("Continue Welsh"),
                    "searchAgainLinkText" -> JsString("Search again Welsh"),
                    "editAddressLinkText" -> JsString("Enter address manually Welsh")
                  )
                ),
                "lookupPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Find address Welsh"),
                    "heading" -> JsString("Find address Welsh"),
                    "afterHeadingText" -> JsString("We will use this address to send letters Welsh"),
                    "filterLabel" -> JsString("Property name or number Welsh (optional)"),
                    "postcodeLabel" -> JsString("Postcode Welsh"),
                    "submitLabel" -> JsString("Find address Welsh"),
                    "noResultsFoundMessage" -> JsString("Sorry, we couldn't find anything for that postcode. Welsh"),
                    "resultLimitExceededMessage" -> JsString("There were too many results. Please add additional details to limit the number of results. Welsh"),
                    "manualAddressLinkText" -> JsString("Enter the address manually Welsh")
                  )
                ),
                "confirmPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Confirm address Welsh"),
                    "heading" -> JsString("Review and confirm Welsh"),
                    "infoSubheading" -> JsString("Your selected address Welsh"),
                    "infoMessage" -> JsString("This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button. Welsh"),
                    "submitLabel" -> JsString("Confirm Address Welsh"),
                    "searchAgainLinkText" -> JsString("Search again Welsh"),
                    "changeLinkText" -> JsString("Edit address Welsh"),
                    "confirmChangeText" -> JsString("By confirming this change, you agree that the information you have given is complete and correct. Welsh")
                  )
                ),
                "editPageLabels" -> JsObject(
                  Seq(
                    "title" -> JsString("Enter address Welsh"),
                    "heading" -> JsString("Enter address Welsh"),
                    "organisationLabel" -> JsString("Organisation (optional) Welsh"),
                    "line1Label" -> JsString("Address line 1 Welsh"),
                    "line2Label" -> JsString("Address line 2 (optional) Welsh"),
                    "line3Label" -> JsString("Address line 3 (optional) Welsh"),
                    "townLabel" -> JsString("Town/City Welsh"),
                    "postcodeLabel" -> JsString("Postcode (optional) Welsh"),
                    "countryLabel" -> JsString("Country Welsh"),
                    "submitLabel" -> JsString("Continue Welsh")
                  )
                )
              )
            )
          )
        )
      )
    )
  }

  // Step 1: Journey start/init
  def initJourney(implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
    import play.api.libs.ws.writeableOf_JsValue
    val payload: JsValue = getAddressJson()

    // TODO: remove this code
    val payloadStr =  Json.stringify(payload)

    Logger("application").info(s"[AddressLookupConnector] - body: ${payloadStr}")

    http.post(url"$addressLookupInitializeUrl")
      .withBody(payload)
      .execute[AddressLookupResponse]
  }

  // Step 2: Extract journey result/outcome
  def getJourneyOutcome(id: String)
                       (implicit hc: HeaderCarrier): Future[AddressLookupJourneyOutcome] = {
    http.get(url"${addressLookupOutcomeUrl(id)}").execute[AddressLookupJourneyOutcome]
  }

}