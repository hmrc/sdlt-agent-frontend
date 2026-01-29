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
import views.html.components.Link

class LinkSpec extends SpecBase with Matchers {

  "Link" - {

    "must render the correct link text in the output HTML with empty prefix/suffix" in new Setup {
      displaysLinkTextCorrectly(link, linkText, linkUrl)
    }

    "must render the correct link text in the output HTML with prefix" in new Setup {
      displaysLinkTextCorrectly(link, linkText, linkUrl, prefixText = prefixText)
    }

    "must render the correct link text in the output HTML with suffix" in new Setup {
      displaysLinkTextCorrectly(link, linkText, linkUrl, suffixText = suffixText)
    }

    "must render the correct link text in the output HTML with prefix/suffix" in new Setup {
      displaysLinkTextCorrectly(link, linkText, linkUrl, prefixText = prefixText, suffixText = suffixText)
    }

    "must render with default class when extraClasses are empty" in new Setup {
      rendersClassesCorrectly(link, linkText, linkUrl, extraClasses = emptyString)
    }

    "must render with default and extra classes when extraClasses are non-empty" in new Setup {
      rendersClassesCorrectly(link, linkText, linkUrl, extraClasses = extraClasses)
    }

    "must add target and rel when isNewTab is true" in new Setup {
      val html: Html = link(linkText, linkUrl, isNewTab = true)
      val linkElement: Elements = getLinkElement(html)

      linkElement.select("a.govuk-link").attr("target") mustBe "_blank"
      linkElement.select("a.govuk-link").attr("rel") mustBe "noreferrer noopener"
    }

    "must not add target and rel when isNewTab is false" in new Setup {
      val html: Html = link(linkText, linkUrl)
      val linkElement: Elements = getLinkElement(html)

      linkElement.select("a.govuk-link").hasAttr("target") mustBe false
      linkElement.select("a.govuk-link").hasAttr("rel") mustBe false
    }

    "must render full stop when linkFullStop is true" in new Setup {
      displaysLinkTextCorrectly(link, linkText, linkUrl, linkFullStop = true)
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

  private def getLinkElement(html: Html): Elements = {
    val doc = Jsoup.parse(html.toString())
    doc.select("p")
  }

  private def displaysLinkTextCorrectly(
                                     link: Link,
                                     linkText: String,
                                     linkUrl: String,
                                     prefixText: String = emptyString,
                                     suffixText: String = emptyString,
                                     linkFullStop: Boolean = false
                                   )(implicit messages: Messages) = {
    val fullStop = if(linkFullStop) "." else emptyString

    val html: Html = link(linkText, linkUrl, false, prefixText, suffixText, emptyString, linkFullStop)
    val linkElement: Elements = getLinkElement(html)

    linkElement.size mustBe 1
    linkElement.text mustBe joinExpectedElements(prefixText, linkText, suffixText) + fullStop
    linkElement.select("a.govuk-link").attr("href") mustBe linkUrl
  }

  private def rendersClassesCorrectly(link: Link, linkText: String, linkUrl: String, extraClasses: String)(implicit messages: Messages) = {
    val html: Html = link(linkText, linkUrl, extraClasses = extraClasses)
    val linkElement: Elements = getLinkElement(html)
    val classes: String = linkElement.attr("class")

    classes.trim mustBe joinExpectedElements("govuk-body", extraClasses)
  }

  private def joinExpectedElements(parts: String*): String =
    parts.filter(_.nonEmpty).mkString(" ")

}
