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

package utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.PaginationLink
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import controllers.manageAgents.routes
import models.AgentDetailsResponse
import utils.mangeAgents.AgentDetailsTestUtil

class PaginationHelperSpec extends AnyWordSpec with Matchers with AgentDetailsTestUtil {

  object TestHelper extends PaginationHelper

  private val testAgentReferenceNumber: String = "ARN001"
  
  private val messagesApi = new DefaultMessagesApi(
    Map(
      "en" -> Map(
        "manageAgents.agentDetails.summaryInfo.text" -> "Showing {0} to {1} of {2} records",
        "pagination.previous"                        -> "Previous",
        "pagination.next"                            -> "Next",
        "site.change"                                -> "Change",
        "site.remove"                                -> "Remove"
      )
    )
  )

  implicit private val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  private val twentyTwoAgents: Seq[AgentDetailsResponse] = getAgentList(22)
  private val nineAgents: Seq[AgentDetailsResponse]      = getAgentList(9)

  "getNumberOfPages" should {
    "return 1 when up to 10 items" in {
      TestHelper.getNumberOfPages(nineAgents.toList) mustBe 1
      TestHelper.getNumberOfPages(twentyTwoAgents.take(10).toList) mustBe 1
    }
    "return ceil(total/10) when more than 10" in {
      TestHelper.getNumberOfPages(twentyTwoAgents.toList) mustBe 3
      TestHelper.getNumberOfPages(twentyTwoAgents.take(11).toList) mustBe 2
      TestHelper.getNumberOfPages(twentyTwoAgents.take(20).toList) mustBe 2
    }
  }

  "getPaginationInfoText" should {
    "return None when list has 10 or fewer items" in {
      TestHelper.getPaginationInfoText(1, nineAgents) mustBe None
      TestHelper.getPaginationInfoText(1, twentyTwoAgents.take(10)) mustBe None
    }
    "return None for invalid page indices (<= 0)" in {
      TestHelper.getPaginationInfoText(0, twentyTwoAgents) mustBe None
      TestHelper.getPaginationInfoText(-1, twentyTwoAgents) mustBe None
    }
    "return 'Showing 1 to 10 of 22 records' on page 1 for 22 items" in {
      TestHelper.getPaginationInfoText(1, twentyTwoAgents) mustBe Some("Showing 1 to 10 of 22 records")
    }
    "return 'Showing 11 to 20 of 22 records' on page 2 for 22 items" in {
      TestHelper.getPaginationInfoText(2, twentyTwoAgents) mustBe Some("Showing 11 to 20 of 22 records")
    }
    "return 'Showing 21 to 22 of 22 records' on page 3 for 22 items" in {
      TestHelper.getPaginationInfoText(3, twentyTwoAgents) mustBe Some("Showing 21 to 22 of 22 records")
    }
    "return None for out-of-range page index" in {
      TestHelper.getPaginationInfoText(4, twentyTwoAgents) mustBe None
    }
  }

  "generateAgentSummary" should {
    "return None for out-of-range page" in {
      TestHelper.generateAgentSummary(0, twentyTwoAgents) mustBe None
      TestHelper.generateAgentSummary(4, twentyTwoAgents) mustBe None
    }
    "return a SummaryList with 10 rows on page 1 when there are 22 items" in {
      val maybeSummary: Option[SummaryList] = TestHelper.generateAgentSummary(1, twentyTwoAgents)
      val summary = maybeSummary.get
      summary.rows.size mustBe 10
      summary.rows.head.key.content.asHtml.body must include ("Agent 1")
      summary.rows.head.value.content.asHtml.body must include ("Address 1")
      summary.rows.head.actions.get.items.size mustBe 2
      val hrefs = summary.rows.head.actions.get.items.map(_.href)
      hrefs.exists(_.contains(routes.CheckYourAnswersController.onPageLoad(Some(testAgentReferenceNumber)).url.stripPrefix("/"))) mustBe true
      hrefs.exists(_.contains(routes.RemoveAgentController.onPageLoad(testAgentReferenceNumber).url.stripPrefix("/"))) mustBe true
    }
    "return a SummaryList with 2 rows on page 3 when there are 22 items" in {
      val summary = TestHelper.generateAgentSummary(3, twentyTwoAgents).get
      summary.rows.size mustBe 2
      summary.rows.head.key.content.asHtml.body must include ("Agent 21")
      summary.rows.last.key.content.asHtml.body must include ("Agent 22")
    }
  }

  "generatePagination" should {
    "return None when only one page" in {
      val res = TestHelper.generatePagination(paginationIndex = 1, numberOfPages = 1)
      res mustBe None
    }
    "return items and prev/next correctly for middle page" in {
      val res = TestHelper.generatePagination(paginationIndex = 2, numberOfPages = 3).get
      val items = res.items.get
      items.length mustBe 3
      items(1).current mustBe Some(true)

      val prev = res.previous.get
      prev.text.get mustBe "Previous"
      prev.href must include ("paginationIndex=1")

      val next = res.next.get
      next.text.get mustBe "Next"
      next.href must include ("paginationIndex=3")
    }
    "omit previous on first page and next on last page" in {
      val first = TestHelper.generatePagination(1, 3).get
      first.previous mustBe None
      first.next.get.href must include ("paginationIndex=2")

      val last = TestHelper.generatePagination(3, 3).get
      last.next mustBe None
      last.previous.get.href must include ("paginationIndex=2")
    }
  }

  "generatePaginationItems" should {
    "produce one item per page with current flag set correctly" in {
      val items = TestHelper.generatePaginationItems(paginationIndex = 2, numberOfPages = 3)
      items.map(_.number.get.mkString) mustBe Seq("1", "2", "3")
      items.map(_.current) mustBe Seq(Some(false), Some(true), Some(false))
      items.head.href must include ("paginationIndex=1")
      items(1).href   must include ("paginationIndex=2")
      items(2).href   must include ("paginationIndex=3")
    }
  }

  "generatePreviousLink / generateNextLink" should {
    "return None for previous on page 1, and None for next on the last page" in {
      TestHelper.generatePreviousLink(1, 3) mustBe None
      TestHelper.generateNextLink(3, 3) mustBe None
    }
    "return proper links on middle page" in {
      val prev: PaginationLink = TestHelper.generatePreviousLink(2, 3).get
      prev.text.get mustBe "Previous"
      prev.href must include ("paginationIndex=1")

      val next: PaginationLink = TestHelper.generateNextLink(2, 3).get
      next.text.get mustBe "Next"
      next.href must include ("paginationIndex=3")
    }
  }
}
