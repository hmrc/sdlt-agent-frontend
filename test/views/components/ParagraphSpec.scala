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
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.EmptyString.emptyString
import views.html.components.Paragraph

class ParagraphSpec extends SpecBase with Matchers {

  "Paragraph" - {

    "must render the correct text in the output HTML" in new Setup {
      displaysCorrectParagraphText(paragraph, paragraphText)
    }

    "must render with default class when extraClasses are empty" in new Setup {
      rendersClassesCorrectly(paragraph, paragraphText, extraClasses = emptyString)
    }

    "must render with default and extra classes when extraClasses are non-empty" in new Setup {
      rendersClassesCorrectly(paragraph, paragraphText, extraClasses = extraClasses)
    }

    "must render with default and bold class when isBold is true" in new Setup {
      rendersClassesCorrectly(paragraph, paragraphText, boldClass)
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

  private def getParagraphElement(html: Html): Elements = {
    val doc = Jsoup.parse(html.toString())
    doc.select("p")
  }

  private def displaysCorrectParagraphText(paragraph: Paragraph, paragraphText: String)(implicit messages: Messages) = {
    val html: Html = paragraph(paragraphText)
    val paragraphElement: Elements = getParagraphElement(html)

    paragraphElement.size mustBe 1
    paragraphElement.text mustBe paragraphText
  }

  private def rendersClassesCorrectly(
                                       paragraph: Paragraph,
                                       paragraphText: String,
                                       extraClasses: String = emptyString,
                                       boldClass: String = emptyString,
                                     )(implicit messages: Messages) = {
    val isBold = if(boldClass.isEmpty) false else true

    val html: Html = paragraph(paragraphText, isBold, extraClasses = extraClasses)
    val paragraphElement: Elements = getParagraphElement(html)
    val classes: String = paragraphElement.attr("class")

    classes.trim mustBe joinExpectedElements("govuk-body", boldClass, extraClasses)
  }

  private def joinExpectedElements(parts: String*): String = {
    println(parts)
    parts.filter(_.nonEmpty).mkString(" ")
  }

}
