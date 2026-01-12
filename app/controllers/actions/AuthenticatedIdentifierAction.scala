/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.actions

import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{Assistant, AuthConnector, AuthProviders,
  AuthorisationException, AuthorisedFunctions, Enrolment, Enrolments, NoActiveSession, User}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.LoggerUtil.logError

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A],
                              block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val defaultPredicate: Predicate = AuthProviders(GovernmentGateway)

    // We expect one to one mapping between AffinityGroup and corresponding Enrollment
    authorised(defaultPredicate)
      .retrieve(
        Retrievals.internalId and
          Retrievals.allEnrolments and
          Retrievals.affinityGroup and
          Retrievals.credentialRole
      ) {
        case Some(internalId) ~ Enrolments(enrolments) ~ Some(Organisation) ~ Some(User) if enrolments.exists(_.key == orgEnrollment) =>
          handleValidEnrollments(block)(request, internalId, enrolments)
        case Some(internalId) ~ Enrolments(enrolments) ~ Some(Agent) ~ Some(User) if enrolments.exists(_.key == agentEnrollment) =>
          handleValidEnrollments(block)(request, internalId, enrolments)
        case Some(_) ~ _ ~ Some(Organisation|Agent) ~ Some(Assistant) => // Not sure if this is really applicable anymore
          logger.error("[AuthenticatedIdentifierAction][authorised] - [Organisation|Agent]: Assistant login attempt")
          // TODO: create UnauthorisedOrganisationAffinityController
          Future.successful(
            Redirect(controllers.routes.UnauthorisedIndividualAffinityController.onPageLoad())
          )
        case Some(_) ~ _ ~ Some(Individual) ~ _ =>
          logger.error("[AuthenticatedIdentifierAction][authorised] - Individual login attempt")
          Future.successful(
            Redirect(controllers.routes.UnauthorisedIndividualAffinityController.onPageLoad())
          )
        case _ =>
          logger.error("[AuthenticatedIdentifierAction][authorised] - authentication failure")
          Future.successful(
            Redirect(routes.AccessDeniedController.onPageLoad()))
      } recover {
      case _: NoActiveSession =>
        logger.error("[AuthenticatedIdentifierAction][authorised] - recover::NoActiveSession")
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        logger.error("[AuthenticatedIdentifierAction][authorised] - recover::AuthorisationException")
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def handleValidEnrollments[A](block: IdentifierRequest[A] => Future[Result])
                                       (request: Request[A], internalId: String, enrollments: Set[Enrolment]) = {
    checkEnrollments(enrollments)
      .map { storn =>
        block(IdentifierRequest(request, internalId, storn))
      }
      .getOrElse(
        Future.successful(
          Redirect(routes.AccessDeniedController.onPageLoad())
        )
      )
  }

  private val orgEnrollment: String = "IR-SDLT-ORG"
  private val agentEnrollment: String = "IR-SDLT-AGENT"

  private val enrolementStornExtractor: Enrolment => Option[String] = (enrolment: Enrolment) =>
    enrolment.identifiers
      .find(id => id.key == "STORN")
      .map(_.value)

  // Always expect enrolments in the input set :: expect STORN key to be the same for Agent and Org
  private def checkEnrollments[A](enrolments: Set[Enrolment]): Option[String] =
    enrolments.find(enrolment => Set(orgEnrollment, agentEnrollment).contains(enrolment.key)) match {
      case Some(enrolment) =>
        (enrolementStornExtractor(enrolment), enrolment.isActivated) match {
          case (Some(storn), true) =>
            Some(storn)
          case (Some(_), false) =>
            logError("[AuthenticatedIdentifierAction][checkEnrollments] - Inactive enrollment")
            None
          case _ =>
            logError("[AuthenticatedIdentifierAction][checkEnrollments] - Unable to retrieve sdlt enrolments")
            None
        }
      case _ =>
        logError("[AuthenticatedIdentifierAction][checkEnrollments] - enrollment not found")
        None
    }

}