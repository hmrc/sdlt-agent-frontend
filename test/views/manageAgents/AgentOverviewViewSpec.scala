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
import forms.manageAgents.RemoveAgentFormProvider
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.manageAgents.{AgentDetailsTestUtil, ViewAssertions}
import views.html.manageAgents.{AgentOverviewView, RemoveAgentView}

class AgentOverviewViewSpec extends SpecBase with ViewAssertions with AgentDetailsTestUtil with GuiceOneAppPerSuite {

  "AgentOverviewView" - {

    "must render the page with correct core html elements" in new Setup {
      val html: Html = view(None, None, None, redirect)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysCorrectTitle(doc, "manageAgents.agentOverview.title")
      displaysCorrectHeadingAndCaption(
        doc,
        "manageAgents.agentDetails.heading",
        "manageAgents.caption",
      )
      hasAddAgentLink(doc)
      hasBackLink(doc)
    }

    // TODO: Tests for zero agents, agents with pagination, flashes, warnings
  }

  trait Setup {
    val redirect: Call = controllers.manageAgents.routes.StartAddAgentController.onPageLoad()
    implicit def request: Request[?] = FakeRequest()
    implicit def messages: Messages  = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val view: AgentOverviewView      = app.injector.instanceOf[AgentOverviewView]
  }

  private def hasAddAgentLink(doc: Document)(implicit messages: Messages) = {
    doc.select("a.govuk-button").text mustBe messages("manageAgents.agentDetails.addAgentButtonText")
  }
}
