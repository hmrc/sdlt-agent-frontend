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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, StornRequiredAction}
import forms.manageAgents.ConfirmAgentContactDetailsFormProvider
import models.NormalMode
import models.manageAgents.ConfirmAgentContactDetails
import models.requests.DataRequest
import navigation.Navigator
import pages.manageAgents.{AgentCheckYourAnswersPage, AgentContactDetailsPage, AgentNamePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.StampDutyLandTaxService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manageAgents.ConfirmAgentContactDetailsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmAgentContactDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       stornRequiredAction: StornRequiredAction,
                                       formProvider: ConfirmAgentContactDetailsFormProvider,
                                       stampDutyLandTaxService: StampDutyLandTaxService,
                                       navigator: Navigator,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ConfirmAgentContactDetailsView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {


  private def getAgentName(implicit request: DataRequest[AnyContent]): Either[Result, String] =
    request.userAnswers.get(AgentNamePage) match {
      case Some(name) => Right(name)
      case None =>
        Left {
          logger.error("Couldn't find agent in user answers")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }


  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      getAgentName match {
        case Left(redirect) => redirect
        case Right(agentName) =>
          val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)
          Ok(view(form, agentName))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen stornRequiredAction).async {
    implicit request =>

      getAgentName match {
        case Right(agentName) =>
          val form: Form[ConfirmAgentContactDetails] = formProvider(agentName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, agentName))),

            {
              case ConfirmAgentContactDetails.Option1 =>
                Future.successful(Redirect(navigator.nextPage(AgentContactDetailsPage, NormalMode, request.userAnswers)))

              case ConfirmAgentContactDetails.Option2 =>
                logger.info(s"[ConfirmAgentContactDetailsController][onSubmit] No agent contact details option selected")
                Future.successful(Redirect(navigator.nextPage(AgentCheckYourAnswersPage, NormalMode, request.userAnswers)))
            }
          )
        case Left(redirect) => Future.successful(redirect)
      }
  }

}
