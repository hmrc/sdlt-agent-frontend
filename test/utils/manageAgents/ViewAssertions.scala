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

package utils.manageAgents

import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.{include, must, mustBe}
import play.api.i18n.Messages

trait ViewAssertions {

  protected def displaysCorrectTitle(doc: Document, title: String, args: Seq[Any] = Seq.empty)(implicit messages: Messages): Assertion = {
    doc.title() must include(messages(title, args: _*))
  }

  protected def displaysCorrectHeading(
                                                  doc: Document,
                                                  heading: String,
                                                  args: Seq[Any] = Seq.empty
                                                )(implicit messages: Messages): Assertion = {
    doc.select("h1.govuk-heading-l").text mustBe messages(heading, args: _*)
  }
  
  protected def displaysCorrectCaption(doc: Document, caption: String)(implicit messages: Messages): Assertion = {
    doc.select("p.govuk-caption-l").text mustBe s"This section is ${messages(caption)}"
  }

  protected def displaysCorrectLabels(doc: Document, labels: Seq[String])(implicit messages: Messages): Assertion = {
    val expectedLabels = doc.select("label")
    
    labels.foreach { key =>
      expectedLabels.text must include(messages(key))
    }
    
    expectedLabels.size mustBe labels.size
  }

  protected def hasCorrectNumOfItems(doc: Document, item: String, num: Int)(implicit messages: Messages): Assertion = {
    doc.select(item).size mustBe num
  }

  protected def hasSubmitButton(doc: Document, text: String)(implicit messages: Messages): Assertion = {
    doc.select("button[type=submit]").text mustBe messages(text)
  }

  protected def hasBackLink(doc: Document)(implicit messages: Messages): Assertion = {
    doc.select("a.govuk-back-link").size() mustBe 1
  }

  protected def displaysErrorSummary(
                                      doc: Document,
                                      errorKeys: Seq[String],
                                      args: Seq[Any] = Seq.empty,
                                    )(implicit messages: Messages): Unit = {
    val errorSummary = doc.select(".govuk-error-summary")
    errorSummary.size mustBe 1
    
    doc.select(".govuk-error-summary__title").text mustBe messages("error.summary.title")

    errorKeys.foreach { key =>
      errorSummary.text() must include(messages(key, args: _*))
    }
  }
}
