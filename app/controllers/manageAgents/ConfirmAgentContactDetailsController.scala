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

import controllers.actions.DataRequiredAction
import controllers.actions.DataRetrievalAction
import controllers.actions.IdentifierAction
import controllers.actions.StornRequiredAction
import forms.manageAgents.ConfirmAgentContactDetailsFormProvider
import models.NormalMode
import models.manageAgents.ConfirmAgentContactDetails
import models.requests.DataRequest
import navigation.Navigator
import pages.manageAgents.AgentCheckYourAnswersPage
import pages.manageAgents.AgentContactDetailsPage
import pages.manageAgents.AgentNamePage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.Result
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggerUtil.logError
import utils.LoggerUtil.logInfo
import views.html.manageAgents.ConfirmAgentContactDetailsView

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class ConfirmAgentContactDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    stornRequiredAction: StornRequiredAction,
    formProvider: ConfirmAgentContactDetailsFormProvider,
    navigator: Navigator,
    val controllerComponents: MessagesControllerComponents,
    view: ConfirmAgentContactDetailsView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def getAgentName(implicit
      request: DataRequest[AnyContent]
  ): Either[Result, String] =
    request.userAnswers.get(AgentNamePage) match {
      case Some(name) => Right(name)
      case None =>
        Left {
          logError("Couldn't find agent in user answers")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      getAgentName match {
        case Left(redirect) => redirect
        case Right(agentName) =>
          val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)
          Ok(view(form, agentName))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen stornRequiredAction)
      .async { implicit request =>
        getAgentName match {
          case Right(agentName) =>
            val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future
                    .successful(BadRequest(view(formWithErrors, agentName))),
                {
                  case ConfirmAgentContactDetails.Option1 =>
                    Future.successful(
                      Redirect(
                        navigator.nextPage(
                          AgentContactDetailsPage,
                          NormalMode,
                          request.userAnswers
                        )
                      )
                    )

                  case ConfirmAgentContactDetails.Option2 =>
                    logInfo(
                      s"[ConfirmAgentContactDetailsController][onSubmit] No agent contact details option selected"
                    )
                    Future.successful(
                      Redirect(
                        navigator.nextPage(
                          AgentCheckYourAnswersPage,
                          NormalMode,
                          request.userAnswers
                        )
                      )
                    )
                }
              )
          case Left(redirect) => Future.successful(redirect)
        }
      }

}
