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

package views

import base.SpecBase
import forms.NoSessionDataFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.manageAgents.ViewSpecBase
import views.html.NoSessionDataView

class NoSessionDataViewSpec extends SpecBase with ViewSpecBase {

  "NoSessionDataView" - {

    "must render the page with correct html elements" in new Setup {
      val html: Html = view(form)
      val doc: Document = Jsoup.parse(html.toString())

      displaysCorrectTitle(doc, "noSessionData.title")
      displaysCorrectHeading(doc, "noSessionData.heading")
      displaysCorrectLabels(
        doc,
        Seq("noSessionData.fileNewReturn", "noSessionData.manageStampTaxes")
      )
      displaysCorrectHint(doc, "noSessionData.hint")
      hasCorrectNumOfItems(doc, ".govuk-radios__item", 2)
      hasSubmitButton(doc, "site.continue")
      hasBackLink(doc)
    }

    "must display error messages when form has errors" in new Setup {
      val errorForm: Form[?] = form
        .withError("value", "noSessionData.error.required")

      val html: Html = view(errorForm)
      val doc: Document = Jsoup.parse(html.toString())

      displaysErrorSummary(
        doc,
        Seq("noSessionData.error.required")
      )
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val formProvider = new NoSessionDataFormProvider()
    val form: Form[?] = formProvider()
    implicit def request: Request[?] = FakeRequest()
    implicit def messages: Messages = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
    val view: NoSessionDataView = app.injector.instanceOf[NoSessionDataView]
  }
}
