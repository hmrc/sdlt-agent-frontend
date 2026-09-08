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

import controllers.actions.*
import forms.manageAgents.AgentNameFormProvider
import models.Mode
import models.manageAgents.AgentName
import navigation.Navigator
import pages.manageAgents.{AgentAddressPage, AgentNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgents.AgentNameView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentNameController@Inject()(
                                    override val messagesApi: MessagesApi,
                                    val controllerComponents: MessagesControllerComponents,
                                    sessionRepository: SessionRepository,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    requireData: DataRequiredAction,
                                    formProvider: AgentNameFormProvider,
                                    stornRequiredAction: StornRequiredAction,
                                    stampDutyLandTaxService: StampDutyLandTaxService,
                                    view: AgentNameView,
                                    navigator: Navigator
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  lazy val form: Form[AgentName] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction) { implicit request =>
    request.userAnswers.get(AgentNamePage) match {
      case None        => Ok(view(form, mode, false))
      case Some(value) => Ok(view(form.fill(AgentName(value)), mode, false))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, false))),
        agentName =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentNamePage, agentName.value))
            isDuplicate    <- stampDutyLandTaxService.isDuplicate(request.storn, agentName.value)
            _              <- sessionRepository.set(updatedAnswers)
          } yield if (isDuplicate && !agentName.continueAnyway) {
            Ok(view(form.fill(AgentName(agentName.value)), mode, true))
          } else {
            Redirect(navigator.nextPage(AgentAddressPage, mode, updatedAnswers))
          }
      )
  }
}
