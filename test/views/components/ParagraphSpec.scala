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
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.Paragraph

class ParagraphSpec extends SpecBase with Matchers {

  "Paragraph" - {

    "must render the given text" in new Setup {
      val html: Html = paragraph(
        paragraphText
      )

      getParagraph(html).text mustBe paragraphText
    }

    "must render only the default body class when no extra classes are supplied" in new Setup {
      val html: Html = paragraph(
        paragraphText
      )

      getParagraph(html).className mustBe "govuk-body"
    }

    "must render default and extraClasses when supplied" in new Setup {
      val html: Html = paragraph(
        paragraphText,
        extraClasses = extraClasses
      )

      val classes: String = getParagraph(html).attr("class")
      classes must include("govuk-body")
      classes must include("govuk-link--inverse")
      classes must include("govuk-link--no-underline")
    }

    "must render default and bold class when isBold is true" in new Setup {
      val html: Html = paragraph(
        paragraphText,
        isBold = true
      )

      val classes: String = getParagraph(html).attr("class")
      classes must include("govuk-body")
      classes must include("govuk-!-font-weight-bold")
    }
  }

  trait Setup {
    val app: Application              = applicationBuilder().build()
    implicit val messages: Messages   = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val paragraph: Paragraph = app.injector.instanceOf[Paragraph]
    val paragraphText: String = "test paragraph text"
    val extraClasses = "govuk-link--inverse govuk-link--no-underline"
    val boldClass = "govuk-!-font-weight-bold"
  }

  private def getParagraph(html: Html) =
    Jsoup.parse(html.toString()).selectFirst("p")

}
