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

package views.manageAgents

import base.SpecBase
import forms.manageAgents.AgentNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.manageAgents.ViewAssertions
import views.html.manageAgents.WarningAgentNameView

class WarningAgentNameViewSpec extends SpecBase with ViewAssertions {

  "WarningAgentNameView" - {

    "must render the page with correct html elements" in new Setup {
      val html: Html = view(form, NormalMode)
      val doc: Document = Jsoup.parse(html.toString())

      displaysCorrectTitle(doc, "manageAgents.agentName.title")
      displaysCorrectHeading(doc, "manageAgents.agentName.heading")
      displaysCorrectCaption(doc, "manageAgents.caption")
      displaysCorrectWarning(doc, "manageAgents.agentName.warning")
      displaysCorrectLabels(doc, Seq("manageAgents.agentName.tip"))
      hasCorrectNumOfItems(doc, "input", 1)
      hasSubmitButton(doc, "site.continue")
      hasBackLink(doc)
    }

    "must display error messages when form has errors" in new Setup {
      val errorForm: Form[?] = form
        .withError("value", "manageAgents.agentName.error.required")
        .withError("value", "manageAgents.agentName.error.invalid")
        .withError("value", "manageAgents.agentName.error.length")

      val html: Html = view(errorForm, NormalMode)
      val doc: Document = Jsoup.parse(html.toString())

      displaysErrorSummary(
        doc,
        Seq(
          "manageAgents.agentName.error.required",
          "manageAgents.agentName.error.invalid",
          "manageAgents.agentName.error.length"
        )
      )
    }
  }

  trait Setup {
    val app: Application             = applicationBuilder().build()
    val formProvider                 = new AgentNameFormProvider()
    val form: Form[?]                = formProvider()
    implicit val request: Request[?] = FakeRequest()
    implicit val messages: Messages  = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val view: WarningAgentNameView   = app.injector.instanceOf[WarningAgentNameView]
  }

  private def displaysCorrectWarning(doc: Document, warningText: String)(implicit messages: Messages) = {
    doc.select(".govuk-warning-text__text").text mustBe s"Warning ${messages(warningText)}"
  }
}

