package utils.manageAgents

import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.{include, must, mustBe}
import play.api.i18n.Messages

trait ViewAssertions {

  protected def displaysCorrectTitle(doc: Document, title: String, args: Seq[Any] = Seq.empty)(implicit messages: Messages): Assertion = {
    doc.title() must include(messages(title, args: _*))
  }

  protected def displaysCorrectHeadingAndCaption(
                                                  doc: Document,
                                                  heading: String,
                                                  caption: String,
                                                  args: Seq[Any] = Seq.empty
                                                )(implicit messages: Messages): Assertion = {
    doc.select("h1.govuk-heading-l").text mustBe messages(heading, args: _*)
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
                                      args: Seq[Any] = Seq.empty
                                    )(implicit messages: Messages): Unit = {
    val errorSummary = doc.select(".govuk-error-summary")
    errorSummary.size mustBe 1

    errorKeys.foreach { key =>
      errorSummary.text() must include(messages(key))
    }
  }
}
