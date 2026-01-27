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
import views.html.manageAgents.RemoveAgentView

class RemoveAgentViewSpec extends SpecBase with ViewAssertions with AgentDetailsTestUtil with GuiceOneAppPerSuite {

  "RemoveAgentView" - {

    "must render the page with correct html elements" in new Setup {
      val html: Html = view(form, postAction, testAgentDetails)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysCorrectTitle(doc, "manageAgents.removeAgent.title")
      displaysCorrectHeadingAndCaption(
        doc,
        "manageAgents.removeAgent.heading",
        "manageAgents.caption",
        Seq(testAgentDetails.name))
      displaysCorrectLabels(doc, Seq("site.yes", "site.no"))
      hasCorrectNumOfItems(doc, ".govuk-radios__item", 2)
      hasSubmitButton(doc, "site.saveAndContinue")
      hasBackLink(doc)
    }

    "must display error messages when form has errors" in new Setup {
      val errorForm: Form[?] = form
        .withError("value", "manageAgents.removeAgent.error.required", testAgentDetails.name)

      val html: Html = view(errorForm, postAction, testAgentDetails)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysErrorSummary(
        doc,
        Seq("manageAgents.removeAgent.error.required"),
        Seq(testAgentDetails.name)
      )
    }
  }

  trait Setup {
    val formProvider                          = new RemoveAgentFormProvider()
    val form: Form[?]                         = formProvider(testAgentDetails)
    val postAction: Call                      = controllers.manageAgents.routes.RemoveAgentController.onSubmit("123")
    implicit def request: Request[?]          = FakeRequest()
    implicit def messages: Messages           = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val view: RemoveAgentView                 = app.injector.instanceOf[RemoveAgentView]
  }
}
