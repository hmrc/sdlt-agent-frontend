/*
 * Copyright 2026 HM Revenue & Customs
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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.SuccessNotificationBanner

class SuccessNotificationBannerSpec extends SpecBase with Matchers {

  "SuccessNotificationBanner" - {

    "must render the correct title" in new Setup {
      val html: Html = successNotificationBanner(bannerText, agentName)
      val title: Element = getNotificationBanner(html).selectFirst(".govuk-notification-banner__title")

      title.text mustBe "Success"
    }

    "must render the correct heading" in new Setup {
      val html: Html = successNotificationBanner(bannerText, agentName)
      val heading: Element = getNotificationBanner(html).selectFirst(".govuk-notification-banner__heading")

      heading.text mustBe messages("manageAgents.agentDetails.submitAgent.notification", agentName)
    }
  }

  trait Setup {
    val app: Application              = applicationBuilder().build()
    implicit val messages: Messages   = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val successNotificationBanner: SuccessNotificationBanner = app.injector.instanceOf[SuccessNotificationBanner]
    val agentName: String = "testAgentName"
    val bannerText: String = "manageAgents.agentDetails.submitAgent.notification"
  }

  def getNotificationBanner(html: Html): Element =
    Jsoup.parse(html.toString()).selectFirst(".govuk-notification-banner")
}
