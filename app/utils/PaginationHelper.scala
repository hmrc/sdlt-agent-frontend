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

import models.AgentDetailsResponse
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, SummaryListViewModel, ValueViewModel}
import controllers.manageAgents.routes.*
import models.requests.DataRequest
import org.apache.pekko.actor.typed.delivery.internal.ProducerControllerImpl.Request

trait PaginationHelper {

  private val ROWS_ON_PAGE = 10
  
  def getPaginationInfoText[A](paginationIndex: Int, itemList: Seq[A])
                              (implicit messages: Messages): Option[String] = {
    
    if (itemList.length <= ROWS_ON_PAGE || paginationIndex <= 0) { None }
    else {
      val paged = itemList.grouped(ROWS_ON_PAGE).toSeq

      paged.lift(paginationIndex - 1).map { detailsChunk =>
        val total = itemList.length
        val start = (paginationIndex - 1) * ROWS_ON_PAGE + 1
        val end = math.min(paginationIndex * ROWS_ON_PAGE, total)
        messages("manageAgents.agentDetails.summaryInfo.text", start, end, total)
      }
    }
  }

  def getNumberOfPages[A](itemList: List[A]): Int =
    itemList
      .grouped(ROWS_ON_PAGE)
      .size

  def generateAgentSummary(paginationIndex: Int, agents: Seq[AgentDetailsResponse])
                          (implicit messages: Messages): Option[SummaryList] = {
    
    val paged: Seq[Seq[AgentDetailsResponse]] = agents.grouped(ROWS_ON_PAGE).toSeq

    val currentPage: Option[Seq[AgentDetailsResponse]] = paged.lift(paginationIndex - 1)

    currentPage.map(pageAgents =>
      SummaryListViewModel(
        rows = pageAgents.map { agentDetails =>
          SummaryListRowViewModel(
            key = KeyViewModel(
              Text(agentDetails.agentName)
            )
              .withCssClass("govuk-!-width-one-third govuk-!-font-weight-regular hmrc-summary-list__key"),
            value = ValueViewModel(
              Text(agentDetails.getFirstLineOfAddress)
            )
              .withCssClass("govuk-summary-list__value govuk-!-width-one-third"),
            actions = Seq(
              ActionItemViewModel(
                Text(messages("site.change")),
                CheckYourAnswersController.onPageLoad().url
              )
                .withVisuallyHiddenText(agentDetails.agentName),
              ActionItemViewModel(
                Text(messages("site.remove")),
                RemoveAgentController.onPageLoad(agentDetails.agentReferenceNumber).url
              )
                .withVisuallyHiddenText(messages(agentDetails.agentName))
            ),
            actionClasses = "govuk-!-width-one-third"
          )
        }
      )
    )
  }

  def generatePagination(paginationIndex: Int, numberOfPages: Int)
                        (implicit messages: Messages): Option[Pagination] =
    if (numberOfPages < 2) None
    else
      Some(
        Pagination(
          items = Some(generatePaginationItems(paginationIndex, numberOfPages)),
          previous = generatePreviousLink(paginationIndex, numberOfPages),
          next = generateNextLink(paginationIndex, numberOfPages),
          landmarkLabel = None,
          classes = "",
          attributes = Map.empty
        )
      )

  def generatePaginationItems(paginationIndex: Int, numberOfPages: Int): Seq[PaginationItem] =
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = AgentOverviewController.onPageLoad(pageIndex).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )

  def generatePreviousLink(paginationIndex: Int, numberOfPages: Int)
                          (implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == 1) None
    else {
      Some(
        PaginationLink(
          href = AgentOverviewController.onPageLoad(paginationIndex - 1).url,
          text = Some(messages("pagination.previous")),
          attributes = Map.empty
        )
      )
    }

  def generateNextLink(paginationIndex: Int, numberOfPages: Int)
                      (implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == numberOfPages) None
    else {
      Some(
        PaginationLink(
          href = AgentOverviewController.onPageLoad(paginationIndex + 1).url,
          text = Some(messages("pagination.next")),
          attributes = Map.empty
        )
      )
    }
}
