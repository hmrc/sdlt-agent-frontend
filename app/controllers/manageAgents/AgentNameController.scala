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

import controllers.actions._
import forms.manageAgents.AgentNameFormProvider
import models.Mode
import navigation.Navigator
import pages.manageAgents.AgentNamePage
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgent.AgentNameView
import scala.concurrent.{ExecutionContext, Future}

class AgentNameController@Inject()(
                                    override val messagesApi: MessagesApi,
                                    val controllerComponents: MessagesControllerComponents,
                                    sessionRepository: SessionRepository,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    requireData: DataRequiredAction,
                                    formProvider: AgentNameFormProvider,
                                    view: AgentNameView,
                                    navigator: Navigator,
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify) { implicit request =>


    Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = ( identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentNamePage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AgentNamePage, mode, updatedAnswers))
      )
  }
}


/**
 class AgentNameController @Inject()(
 override val messagesApi: MessagesApi,
 val controllerComponents: MessagesControllerComponents,
 sessionRepository: SessionRepository,
 identify: IdentifierAction,
 getData: DataRetrievalAction,
 requireData: DataRequiredAction,
 formProvider: AgentNameFormProvider,
 view: AgentNameView,
 navigator: Navigator,
 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

 val form = formProvider()

 def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData){
 implicit request =>
 val form = formProvider()
 Ok(view(form, mode))
 //Ok(view(preparedForm, mode))
 }

 def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
 implicit request =>
 form
 .bindFromRequest()
 .fold(
 //          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
 formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
 value =>
 for {
 updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentNamePage, value))
 _              <- sessionRepository.set(updatedAnswers)
 } yield Redirect(navigator.nextPage(AgentNamePage, mode, updatedAnswers))
 )
 }
 }
*/