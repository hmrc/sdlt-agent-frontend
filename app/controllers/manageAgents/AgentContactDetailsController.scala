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

import controllers.actions.IdentifierAction
import models.Mode
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import controllers.JourneyRecoveryController
import controllers.actions.*
import forms.mappings.manageAgents.AgentContactDetailsFormProvider

import javax.inject.Inject
import navigation.Navigator
import models.{Mode, UserAnswers}
import pages.manageAgents.AgentContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
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

    val form = formProvider()

  // TODO Change back to (identify andThen getData andThen requireData) after main merge
    def onPageLoad(mode: Mode, storn: String): Action[AnyContent] = Action {
      implicit request =>

        val userAnswersOpt: Option[UserAnswers] = request.session.get("userAnswers").map { jsonStr =>
          Json.parse(jsonStr).as[UserAnswers]
        }

        val preparedForm = userAnswersOpt.flatMap(_.get(AgentContactDetailsPage)) match {
          case None => form
          case Some(value) => form.fill((Some(""), Some("")))
        }

        Ok(view(preparedForm, mode, storn))
    }

  // TODO Change back to (identify andThen getData andThen requireData) after main merge
    def onSubmit(mode: Mode, storn: String): Action[AnyContent] = Action.async { implicit request =>
      val userAnswersOpt = request.session.get("userAnswers").map(Json.parse(_).as[UserAnswers])

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, storn))),

        value =>
          userAnswersOpt match {
            case Some(userAnswers) =>
              val updatedAnswers = userAnswers.set(AgentContactDetailsPage, true).get
              for {
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AgentContactDetailsPage, mode, updatedAnswers, storn))

            case None =>
              Future.successful(Redirect(controllers.routes.CheckYourAnswersController.onPageLoad()))
          }
      )
    }

}