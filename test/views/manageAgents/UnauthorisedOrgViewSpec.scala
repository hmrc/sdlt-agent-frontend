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

package views.manageAgents

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.manageAgents.ViewAssertions
import views.html.manageAgents.UnauthorisedOrgView

class UnauthorisedOrgViewSpec extends SpecBase with ViewAssertions with GuiceOneAppPerSuite {

  "UnauthorisedOrgView" - {

    "must render the page with correct html elements" in new Setup {
      val html: Html = view()
      val doc: Document = Jsoup.parse(html.toString())

      displaysCorrectTitle(doc, "manageAgents.unauthorised.org.title")
      displaysCorrectHeading(doc, "manageAgents.unauthorised.org.heading")
    }
  }

  trait Setup {
    implicit val request: Request[?] = FakeRequest()
    implicit val messages: Messages  = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    implicit lazy val applicationConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val view: UnauthorisedOrgView    = app.injector.instanceOf[UnauthorisedOrgView]
  }
}

