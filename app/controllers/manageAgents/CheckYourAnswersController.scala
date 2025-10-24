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

package controllers.manageAgents

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist.*
import viewmodels.manageAgents.checkAnswers.*
import views.html.manageAgents.CheckYourAnswersView

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
//                                            getData: DataRetrievalAction,
//                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify { // (identify andThen getData andThen requireData)
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          AnswerSummary.row("John Doe", "agentName"),
          AnswerSummary.row("123 Nowhere Lane", "address"),
          AnswerSummary.row("07123456789", "contactTelephoneNumber"),
          AnswerSummary.row("john.doe@example.com", "contactEmail")
        ).flatten
      )

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = identify { implicit request => // (identify andThen getData andThen requireData)
    NoContent
  }
}
