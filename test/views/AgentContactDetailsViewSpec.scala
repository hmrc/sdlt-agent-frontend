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

import forms.mappings.manageAgents.AgentContactDetailsFormProvider
import models.CheckMode
import org.apache.pekko.actor.setup.Setup
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.govuk.all.{InputViewModel, LabelViewModel}
import views.html.helper.form
import views.html.manageAgents.AgentContactDetailsView

class AgentContactDetailsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val storn: String = "STN001"
  lazy val AgentContactDetailsRoute = controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(CheckMode, storn).url

  trait Setup {
    val formProvider = new AgentContactDetailsFormProvider()
    val form: Form[?] = formProvider()
    implicit val request: Request[?] = FakeRequest()
    implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])

    val view: AgentContactDetailsView = app.injector.instanceOf[AgentContactDetailsView]
  }

  "AgentContactDetailsView" should {
    "render the page with title and heading" in new Setup {
      val html = view(form, CheckMode, storn)
      val doc = org.jsoup.Jsoup.parse(html.toString())

      doc.select("span.govuk-caption-xl").text() mustBe messages("manageAgents.agentContactDetails.caption")
      doc.select("h1").text() mustBe messages("manageAgents.agentContactDetails.heading")
    }
  }

  "display error messages when form has errors" in new Setup {
    val errorForm = form
      .withError("phone", "manageAgents.agentContactDetails.error.phoneLength")
      .withError("email", "manageAgents.agentContactDetails.error.emailLength")
      .withError("email", "manageAgents.agentContactDetails.error.emailInvalid")

    val html = view(errorForm, CheckMode, storn)
    val doc = org.jsoup.Jsoup.parse(html.toString())
    val errorSummaryText = doc.select(".govuk-error-summary").text()

    errorSummaryText must include(messages("manageAgents.agentContactDetails.error.phoneLength"))
    errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailLength"))
    errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailInvalid"))
  }
}

