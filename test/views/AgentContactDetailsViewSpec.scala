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

import forms.manageAgents.AgentContactDetailsFormProvider
import models.NormalMode
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.manageAgents.AgentContactDetailsView

class AgentContactDetailsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  "AgentContactDetailsView" should {

    "render the page with correct html elements" in new Setup {
      val html: Html = view(form, NormalMode, "testAgentName")
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      doc.title() must include(messages("manageAgents.agentContactDetails.title"))
      doc.select("h1.govuk-heading-l").text mustBe messages("manageAgents.agentContactDetails.heading", "testAgentName")
      doc.select("p.govuk-caption-l").text mustBe s"This section is ${messages("manageAgents.caption")}"
      doc.select("label").get(0).text mustBe messages("manageAgents.agentContactDetails.label1")
      doc.select("label").get(1).text mustBe messages("manageAgents.agentContactDetails.label2")
      doc.select("label").size mustBe 2
      doc.select("input").size mustBe 2
      doc.select("div.govuk-hint").text mustBe messages("manageAgents.agentContactDetails.hint")
      doc.select("button[type=submit]").text mustBe messages("site.continue")
    }

    "display error messages when form has errors" in new Setup {
      val errorForm: Form[?] = form
        .withError("phone", "manageAgents.agentContactDetails.error.phoneLength")
        .withError("phone", "manageAgents.agentContactDetails.error.phoneInvalid")
        .withError("phone", "manageAgents.agentContactDetails.error.phoneInvalidFormat")
        .withError("email", "manageAgents.agentContactDetails.error.emailLength")
        .withError("email", "manageAgents.agentContactDetails.error.emailInvalid")
        .withError("email", "manageAgents.agentContactDetails.error.emailInvalidFormat")

      val html: Html = view(errorForm, NormalMode, "testAgentName")
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())
      val errorSummaryText: String = doc.select(".govuk-error-summary").text()

      errorSummaryText must include(messages("manageAgents.agentContactDetails.error.phoneLength"))
      errorSummaryText must include(messages("manageAgents.agentContactDetails.error.phoneInvalid"))
      errorSummaryText must include(messages("manageAgents.agentContactDetails.error.phoneInvalidFormat"))
      errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailLength"))
      errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailInvalid"))
      errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailInvalidFormat"))
    }
  }

  trait Setup {
    val formProvider = new AgentContactDetailsFormProvider()
    val form: Form[?] = formProvider("agent name")
    implicit val request: Request[?] = FakeRequest()
    implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])

    val view: AgentContactDetailsView = app.injector.instanceOf[AgentContactDetailsView]
  }
}

