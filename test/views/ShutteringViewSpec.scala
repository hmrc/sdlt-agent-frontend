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

package views

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.ShutteringView

class ShutteringViewSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {
  
  trait Setup {

    implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit val appConfig: FrontendAppConfig = new FrontendAppConfig(app.configuration)

    implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit lazy val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

    def parseHtml(html: Html) = Jsoup.parse(html.toString)

    val view: ShutteringView = app.injector.instanceOf[ShutteringView]
  }

  "ShutteringView" - {
    "render the page with correct title and heading" in new Setup {
      val html = view()
      val doc = parseHtml(html)

      val heading = doc.select("h1.govuk-heading-l")

      heading.size() mustBe 1
      heading.text() mustBe messages("shuttering.heading")
      doc.title() must include(messages("shuttering.title"))
    }

    "render the page with paragraph" in new Setup {
      val html = view()
      val doc = parseHtml(html)

      val paragraph = doc.select("p.govuk-body")

      paragraph.size() mustBe 1
      paragraph.text() must include(messages("shuttering.p1", appConfig.scheduledDeploymentDate, appConfig.scheduledDeploymentTime))
    }
  }
}