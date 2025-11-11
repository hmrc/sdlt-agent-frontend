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
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage, AgentNamePage}
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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


  
  
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      
      val agentName = request.userAnswers.get(AgentNamePage) match{
        case None => ""
        case Some(value) => value
      }
      val form = formProvider(agentName)
      
      val preparedForm = request.userAnswers.get(AgentContactDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, agentName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val agentName = request.userAnswers.get(AgentNamePage) match{
        case None => ""
        case Some(value) => value
      }
      val form = formProvider(agentName)
      
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, agentName))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentContactDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AgentCheckYourAnswersPage, mode, updatedAnswers))
      )
  }
}