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
import org.jsoup.nodes.Element
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.EmptyString.emptyString
import views.html.components.Link

class LinkSpec extends SpecBase with Matchers {

  "Link" - {
    
    "must render the link text inside an anchor" in new Setup {
      val html: Html = link(linkText, linkUrl)

      val a: Element = getParagraph(html).selectFirst("a.govuk-link")
      a.text mustBe linkText
    }

    "must render the correct href attribute" in new Setup {
      val html: Html = link(linkText, linkUrl)

      val a: Element = getParagraph(html).selectFirst("a.govuk-link")
      a.attr("href") mustBe linkUrl
    }

    "must render prefix text before the link when provided" in new Setup {
      val html: Html = link(
        linkText,
        linkUrl,
        prefixTextKey = prefixText
      )

      getParagraph(html).text must startWith(prefixText)
    }

    "must render suffix text after the link when provided" in new Setup {
      val html: Html = link(
        linkText,
        linkUrl,
        suffixTextKey = suffixText
      )

      getParagraph(html).text must endWith(suffixText)
    }

    "must not render prefix or suffix when they are empty" in new Setup {
      val html: Html = link(linkText, linkUrl)

      getParagraph(html).text mustBe linkText
    }

    "must append a full stop outside the link when enabled" in new Setup {
      val html: Html = link(
        linkText,
        linkUrl,
        linkFullStop = true
      )

      val p: Element = getParagraph(html)
      val a: Element = p.selectFirst("a")

      a.text mustBe linkText
      p.text mustBe s"$linkText."
    }

    "must render only the default body class when no extra classes are supplied" in new Setup {
      val html: Html = link(
        linkText,
        linkUrl,
        extraClasses = emptyString
      )

      getParagraph(html).className mustBe "govuk-body"
    }

    "must render default and extra classes when supplied" in new Setup {
      val html: Html = link(
        linkText,
        linkUrl,
        extraClasses = extraClasses
      )

      val classes: String = getParagraph(html).attr("class")
      classes must include("govuk-body")
      classes must include("govuk-link--inverse")
      classes must include("govuk-link--no-underline")
    }

    "must add target and rel attributes when isNewTab is true" in new Setup {
      val html: Html = link(
        linkText,
        linkUrl,
        isNewTab = true
      )

      val a: Element = getParagraph(html).selectFirst("a")
      a.attr("target") mustBe "_blank"
      a.attr("rel") mustBe "noreferrer noopener"
    }

    "must not add target or rel attributes when isNewTab is false" in new Setup {
      val html: Html = link(linkText, linkUrl)

      val a: Element = getParagraph(html).selectFirst("a")
      a.hasAttr("target") mustBe false
      a.hasAttr("rel") mustBe false
    }
  }

  trait Setup {
    val app: Application              = applicationBuilder().build()
    implicit val messages: Messages   = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
    val link: Link = app.injector.instanceOf[Link]
    val linkText: String = "testLinkText"
    val linkUrl = "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
    val prefixText = "testPrefixText"
    val suffixText = "testSuffixText"
    val extraClasses = "govuk-link--inverse govuk-link--no-underline"
  }

  private def getParagraph(html: Html) =
    Jsoup.parse(html.toString()).selectFirst("p")
}
