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

package controllers.actions

import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
    override val authConnector: AuthConnector,
    config: FrontendAppConfig,
    val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](
      request: Request[A],
      block: IdentifierRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val defaultPredicate: Predicate = AuthProviders(GovernmentGateway)

    authorised(defaultPredicate)
      .retrieve(
        Retrievals.internalId and
          Retrievals.allEnrolments and
          Retrievals.affinityGroup and
          Retrievals.credentialRole
      ) {
        // TODO: Add more cases to log and handle error response for missing items eg missing Organisation
        case Some(internalId) ~ Enrolments(enrolments) ~ Some(
              Organisation
            ) ~ Some(User) if enrolments.exists(_.key == orgEnrolment) =>
          hanldeValidEnrolments(block)(request, internalId, enrolments)

        case Some(internalId) ~ Enrolments(enrolments) ~ Some(Agent) ~ Some(
              User
            ) if enrolments.exists(_.key == agentEnrolment) =>
          hanldeValidEnrolments(block)(request, internalId, enrolments)

        case Some(_) ~ _ ~ Some(Organisation | Agent) ~ Some(Assistant) =>
          logger.error(
            "[AuthenticatedIdentifierAction][unauthorised] - [Organisation|Agent]: Assistant login attempt"
          )
          Future.successful(
            Redirect(
              controllers.manageAgents.routes.UnauthorisedOrganisationAffinityController
                .onPageLoad()
            )
          )

        case Some(_) ~ _ ~ Some(Individual) ~ _ =>
          logger.error(
            "[AuthenticatedIdentifierAction][unauthorised] - Individual login attempt"
          )
          Future.successful(
            Redirect(
              controllers.manageAgents.routes.UnauthorisedIndividualAffinityController
                .onPageLoad()
            )
          )
        case _ =>
          logger.error(
            "[AuthenticatedIdentifierAction][unauthorised] - authentication failure"
          )
          Future.successful(
            Redirect(routes.AccessDeniedController.onPageLoad())
          )
      } recover {
      case _: NoActiveSession =>
        Redirect(
          config.loginUrl,
          Map("continue" -> Seq(config.loginContinueUrl))
        )
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def hanldeValidEnrolments[A](
      block: IdentifierRequest[A] => Future[Result]
  )(request: Request[A], internalId: String, enrollments: Set[Enrolment]) = {
    hasSdltOrgEnrolment(enrollments)
      .map { storn =>
        block(IdentifierRequest(request, internalId, storn))
      }
      .getOrElse(
        Future.successful(
          Redirect(routes.AccessDeniedController.onPageLoad())
        )
      )
  }

  private val enrolmentStornExtractor: Enrolment => Option[String] =
    (enrolment: Enrolment) =>
      enrolment.identifiers
        .find(id => id.key == "STORN")
        .map(_.value)

  private val orgEnrolment: String = "IR-SDLT-ORG"
  private val agentEnrolment: String = "IR-SDLT-AGENT"

  private def hasSdltOrgEnrolment[A](
      enrolments: Set[Enrolment]
  ): Option[String] =
    enrolments.find(e =>
      Set(orgEnrolment, agentEnrolment).contains(e.key)
    ) match {
      case Some(enrolment) =>
        (
          enrolmentStornExtractor(enrolment),
          enrolment.state.toLowerCase()
        ) match {
          case (Some(storn), "activated" | "notyetactivated") =>
            Some(storn)
          case _ =>
            logger.error(
              "EnrolmentAuthIdentifierAction - Unable to retrieve sdlt enrolments"
            )
            None
        }
      case _ => None
    }
}
