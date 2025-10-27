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

import models.AgentDetails
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, SummaryListViewModel, ValueViewModel}

trait PaginationHelper {

  private val ROWS_ON_PAGE = 10

  def getPaginationInfoText(paginationIndex: Int, agentsList: Seq[AgentDetails])
                           (implicit messages: Messages): Option[Html] = {
    if (agentsList.isEmpty || paginationIndex <= 0) return None

    val paged = agentsList.grouped(ROWS_ON_PAGE).toSeq

    paged.lift(paginationIndex - 1).map { detailsChunk =>
      val total = agentsList.length
      val start = (paginationIndex - 1) * ROWS_ON_PAGE + 1
      val end = math.min(paginationIndex * ROWS_ON_PAGE, total)

      Html(
        s"""
           |<p class="govuk-body>${messages("manageAgents.agentDetails.summaryInfo.partOne")} $start ${messages("manageAgents.agentDetails.summaryInfo.partTwo")} $end ${messages("manageAgents.agentDetails.summaryInfo.partThree")} $total
           |</p>
           |""".stripMargin
      )
    }
  }

  def getNumberOfPages(agentDetailsList: List[AgentDetails]): Int = agentDetailsList.grouped(ROWS_ON_PAGE).size

  def generateAgentSummary(paginationIndex: Int, agents: Seq[AgentDetails])
                          (implicit messages: Messages): Option[SummaryList] = {
    
    val paged: Seq[Seq[AgentDetails]] = agents.grouped(ROWS_ON_PAGE).toSeq

    val currentPage: Option[Seq[AgentDetails]] = paged.lift(paginationIndex - 1)

    currentPage.map(pageAgents =>
      SummaryListViewModel(
        rows = pageAgents.map { agentDetails =>
          SummaryListRowViewModel(
            key = KeyViewModel(
              Text(agentDetails.name)
            )
              .withCssClass(
                "govuk-!-width-one-third " +
                  "govuk-!-font-weight-regular " +
                  "hmrc-summary-list__key"
              ),
            value = ValueViewModel(
              Text(agentDetails.getFirstLineOfAddress)
            )
              .withCssClass(
                "govuk-summary-list__value" +
                  "govuk-!-width-one-third"
              ),
            actions = Seq(
              ActionItemViewModel(
                Text(messages("site.change")),
                controllers.manageAgents.routes.CheckYourAnswersController.onPageLoad(agentDetails.storn).url
              )
                .withVisuallyHiddenText(messages("manageAgents.agentOverview.change.visuallyHidden")),
              ActionItemViewModel(
                Text(messages("site.remove")),
                controllers.manageAgents.routes.RemoveAgentController.onPageLoad(agentDetails.storn).url
              )
                .withVisuallyHiddenText(messages("manageAgents.agentOverview.remove.visuallyHidden"))
            ),
            actionClasses = "govuk-!-width-one-third"
          )
        }
      )
    )
  }
  
  def generatePagination(storn: String, paginationIndex: Int, numberOfPages: Int)(implicit messages: Messages): Option[Pagination] =
    if (numberOfPages < 2) None
    else
      Some(
        Pagination(
          items = Some(generatePaginationItems(storn, paginationIndex, numberOfPages)),
          previous = generatePreviousLink(storn, paginationIndex, numberOfPages),
          next = generateNextLink(storn, paginationIndex, numberOfPages),
          landmarkLabel = None,
          classes = "",
          attributes = Map.empty
        )
      )

  def generatePaginationItems(storn: String, paginationIndex: Int, numberOfPages: Int): Seq[PaginationItem] =
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = controllers.manageAgents.routes.AgentOverviewController.onPageLoad(storn, pageIndex).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )

  def generatePreviousLink(storn: String, paginationIndex: Int, numberOfPages: Int)(implicit
                                                                     messages: Messages
  ): Option[PaginationLink] =
    if (paginationIndex == 1) None
    else {
      Some(
        PaginationLink(
          href = controllers.manageAgents.routes.AgentOverviewController.onPageLoad(storn, paginationIndex - 1).url,
          text = Some(messages("pagination.previous")),
          attributes = Map.empty
        )
      )
    }

  def generateNextLink(storn: String, paginationIndex: Int, numberOfPages: Int)(implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == numberOfPages) None
    else {
      Some(
        PaginationLink(
          href = controllers.manageAgents.routes.AgentOverviewController.onPageLoad(storn, paginationIndex + 1).url,
          text = Some(messages("pagination.next")),
          attributes = Map.empty
        )
      )
    }

  
  
}
