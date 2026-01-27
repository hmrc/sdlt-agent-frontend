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
import forms.manageAgents.AgentContactDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.manageAgents.ViewAssertions
import views.html.manageAgents.AgentContactDetailsView

class AgentContactDetailsViewSpec extends SpecBase with ViewAssertions with GuiceOneAppPerSuite {

  "AgentContactDetailsView" - {

    "must render the page with correct html elements" in new Setup {
      val html: Html = view(form, NormalMode, "testAgentName")
      val doc: Document = Jsoup.parse(html.toString())

      displaysCorrectTitle(doc, "manageAgents.agentContactDetails.title")
      displaysCorrectHeading(doc, "manageAgents.agentContactDetails.heading", Seq("testAgentName"))
      displaysCorrectCaption(doc, "manageAgents.caption")
      displaysCorrectLabels(doc, Seq("manageAgents.agentContactDetails.label1", "manageAgents.agentContactDetails.label2"))
      hasCorrectNumOfItems(doc, "input", 2)
      hasSubmitButton(doc, "site.continue")
      hasBackLink(doc)
    }

    "must display error messages when form has errors" in new Setup {
      val errorForm: Form[?] = form
        .withError("phone", "manageAgents.agentContactDetails.error.phoneLength")
        .withError("phone", "manageAgents.agentContactDetails.error.phoneInvalid")
        .withError("phone", "manageAgents.agentContactDetails.error.phoneInvalidFormat")
        .withError("email", "manageAgents.agentContactDetails.error.emailLength")
        .withError("email", "manageAgents.agentContactDetails.error.emailInvalid")
        .withError("email", "manageAgents.agentContactDetails.error.emailInvalidFormat")

      val html: Html = view(errorForm, NormalMode, "testAgentName")
      val doc: Document = Jsoup.parse(html.toString())

      displaysErrorSummary(
        doc,
        Seq(
          "manageAgents.agentContactDetails.error.phoneLength",
          "manageAgents.agentContactDetails.error.phoneInvalid",
          "manageAgents.agentContactDetails.error.phoneInvalidFormat",
          "manageAgents.agentContactDetails.error.emailLength",
          "manageAgents.agentContactDetails.error.emailInvalid",
          "manageAgents.agentContactDetails.error.emailInvalidFormat"
        )
      )
    }
  }

  trait Setup {
    val formProvider                  = new AgentContactDetailsFormProvider()
    val form: Form[?]                 = formProvider("agent name")
    implicit val request: Request[?]  = FakeRequest()
    implicit val messages: Messages   = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val view: AgentContactDetailsView = app.injector.instanceOf[AgentContactDetailsView]
  }
}

