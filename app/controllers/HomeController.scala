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

package controllers

import controllers.actions.*
import models.NormalMode
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.Future

class HomeController @Inject()(
                                override val messagesApi: MessagesApi,
                                identify: IdentifierAction,
                                getData: DataRetrievalAction,
                                val controllerComponents: MessagesControllerComponents
                              ) extends FrontendBaseController with I18nSupport {

  // TODO: TO BE REMOVED

  // TODO: This is dummy page which we use temporarily to redirect to on successful submission of a form

  // TODO: This is dummy page is also used as a temporary landing page - as the landing page has not been implemented yet
  
  // TODO: We can temporarily use this page to show the pages we have implemented

  def onPageLoad(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val removeAgentUrl = controllers.manageAgents.routes.RemoveAgentController.onSubmit().url
      val agentNameURL = controllers.manageAgents.routes.AgentNameController.onPageLoad(NormalMode).url
      val agentOverviewUrl = controllers.manageAgents.routes.AgentOverviewController.onPageLoad(1).url

        Future.successful(Ok(Html(
          s"""
            |<h1> removeAgentUrl: </h1> <a href=$removeAgentUrl> $removeAgentUrl </a>
            |<h1> agentNameURL: </h1> <a href=$agentNameURL> $agentNameURL </a>
            |<h1> agentOverviewUrl: </h1> <a href=$agentOverviewUrl> $agentOverviewUrl </a>
            |""".stripMargin
        )))
  }
}
