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

import javax.inject.Singleton
import controllers.actions.*
import forms.manageAgents.AgentContactDetailsFormProvider
import models.manageAgents.AgentContactDetails

import javax.inject.Inject
import navigation.Navigator
import models.Mode
import models.requests.DataRequest
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage, AgentNamePage, AgentReferenceNumberPage}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgents.AgentContactDetailsView

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AgentContactDetailsController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               sessionRepository: SessionRepository,
                                               navigator: Navigator,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AgentContactDetailsFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: AgentContactDetailsView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  private def getAgentName(implicit request: DataRequest[AnyContent]): Either[Result, String] =
    request.userAnswers.get(AgentNamePage) match {
      case Some(name) => Right(name)
      case None =>
        Left {
          logger.error("Agent name not found in user answers")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
  
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      getAgentName match {
        case Left(redirect) => redirect
        case Right(agentName) =>
          val form = formProvider(agentName)
          val preparedForm = request.userAnswers.get(AgentContactDetailsPage)
            .fold(form)(form.fill)
          Ok(view(preparedForm, mode, agentName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      getAgentName match {
        case Left(redirect) => Future.successful(redirect)
        case Right(agentName) =>
          val form = formProvider(agentName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, agentName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentContactDetailsPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AgentCheckYourAnswersPage, mode, updatedAnswers, request.userAnswers.get(AgentReferenceNumberPage)))
          )
      }
  }
}