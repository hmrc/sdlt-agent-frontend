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
import models.responses.organisation.CreatedAgent
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import utils.PaginationHelper
import utils.manageAgents.{AgentDetailsTestUtil, ViewAssertions}
import views.html.manageAgents.AgentOverviewView

class AgentOverviewViewSpec extends SpecBase with ViewAssertions with AgentDetailsTestUtil with PaginationHelper with GuiceOneAppPerSuite {

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

    "must render the correct html elements when there are no agents" in new Setup {
      val html: Html = view(None, None, None, redirect)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysCorrectInfoText(doc, false)
      displaysNoAgentsListedText(doc)
      paginationDoesNotExist(doc)
    }

    "must render the correct html elements when there are more than 0 but less than 11 agents" in new Setup {
      private val sevenAgents = getAgentList(7)
      private val agentSummary = generateAgentSummary(1, sevenAgents)

      val html: Html = view(agentSummary, None, None, redirect)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysCorrectInfoText(doc, true)
      displaysSummaryListWithCorrectRowsAndValues(doc, sevenAgents)
      paginationDoesNotExist(doc)
    }

    "must render the correct html elements when there are 11 or more agents" in new Setup {
      private val twentyTwoAgents = getAgentList(22)
      private val agentSummary = generateAgentSummary(1, twentyTwoAgents)
      private val pagination = generatePagination(1, 3)
      private val paginationText = getPaginationInfoText(1, twentyTwoAgents)

      val html: Html = view(agentSummary, pagination, paginationText, redirect)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysCorrectInfoText(doc, true)
      displaysCorrectPaginationInfoText(doc, twentyTwoAgents.size)
      displaysSummaryListWithCorrectRowsAndValues(doc, twentyTwoAgents)
      paginationExistsAndDisplaysCorrectly(doc)
    }

    "must render the page with success flashes" in new Setup {
      private val requestWithFlash =
        FakeRequest()
          .withFlash("agentUpdated" -> messages("manageAgents.agentDetails.updateAgent.notification", "testName"))
          .withFlash("agentRemoved" -> messages("manageAgents.agentDetails.removeAgent.notification", "testName"))
          .withFlash("agentCreated" -> messages("manageAgents.agentDetails.submitAgent.notification", "testName"))

      val html: Html = view(None, None, None, redirect)(requestWithFlash, messages)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysFlashes(
        doc,
        Seq(
          "manageAgents.agentDetails.updateAgent.notification",
          "manageAgents.agentDetails.removeAgent.notification",
          "manageAgents.agentDetails.submitAgent.notification"
        )
      )
    }

    "must render the page with error flash" in new Setup {
      private val requestWithFlash =
        FakeRequest()
          .withFlash("agentsLimitReached" -> messages("manageAgents.agentDetails.limitReached"))

      val html: Html = view(None, None, None, redirect)(requestWithFlash, messages)
      val doc: Document = org.jsoup.Jsoup.parse(html.toString())

      displaysErrorSummary(
        doc,
        Seq(
          "manageAgents.agentDetails.limitReached"
        )
      )
    }
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

  private def displaysCorrectInfoText(doc: Document, agentsExist: Boolean)(implicit messages: Messages) = {
    if (agentsExist) {
      doc.select("p.govuk-body").get(0).text mustBe messages("manageAgents.agentOverview.nonZeroAgents.info.text")
    } else {
      doc.select("p.govuk-body").get(0).text mustBe messages("manageAgents.agentOverview.noAgents.info.text")
    }
  }

  private def displaysNoAgentsListedText(doc: Document)(implicit messages: Messages) = {
    doc.select("p.govuk-body").get(1).text mustBe messages("manageAgents.agentOverview.noAgents.text")
  }

  private def displaysCorrectPaginationInfoText(doc: Document, numOfAgents: Int)(implicit messages: Messages) = {
    doc.select("p.govuk-body").get(1).text mustBe s"Showing 1 to 10 of $numOfAgents records"
  }

  private def displaysSummaryListWithCorrectRowsAndValues(doc:Document, agents: List[CreatedAgent]) = {
    doc.select(".govuk-summary-list").size() mustBe 1

    val rows = doc.select(".govuk-summary-list__row")
    val expectedRows = if(agents.size > 10) 10 else agents.size
    rows.size() mustBe expectedRows

    val firstAgent = agents.head

    doc.select(".govuk-summary-list__key").text() must include(firstAgent.name)
    doc.select(".govuk-summary-list__value").text() must include(firstAgent.getAddressWithHouseNumberLegacy)
    doc.select(".govuk-summary-list__actions-list-item").text() must include("Change")
    doc.select(".govuk-summary-list__actions-list-item").text() must include("Remove")
  }

  private def paginationDoesNotExist(doc: Document) = {
    doc.select(".govuk-pagination").isEmpty mustBe true
  }

  private def paginationExistsAndDisplaysCorrectly(doc: Document) = {
    doc.select(".govuk-pagination").isEmpty mustBe false

    val paginationLinks = doc.select(".govuk-pagination__link").map(_.text.trim)

    paginationLinks must contain allOf ("1", "2", "3", "Next")
  }

  private def displaysFlashes(doc: Document, flashKeys: Seq[String])(implicit messages: Messages): Unit = {
    val flashes = doc.select(".govuk-notification-banner")

    doc.select("h2.govuk-notification-banner__title").text must include("Success")

    flashKeys.foreach { key =>
      flashes.text must include(messages(flashKeys, "testName"))
    }
  }
}
