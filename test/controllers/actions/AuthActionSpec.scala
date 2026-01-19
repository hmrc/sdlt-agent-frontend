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

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.TestAuthRetrievals.Ops
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  trait Fixture {

    val id: String = UUID.randomUUID().toString

    val testStorn: String = "STN001"

    val mockAuthConnector: AuthConnector = mock[AuthConnector]

    val application: Application = applicationBuilder(userAnswers = None)
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
      .build()

    val bodyParsers:BodyParsers.Default = application.injector.instanceOf[BodyParsers.Default]
    val appConfig:FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

    val orgActiveEnrolment: Enrolment = Enrolment(
      "IR-SDLT-ORG",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "activated",
      None
    )
    val orgInActiveEnrolment: Enrolment = Enrolment(
      "IR-SDLT-ORG",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "inactivated",
      None
    )
    val orgNotYetActivatedEnrolment: Enrolment = Enrolment(
      "IR-SDLT-ORG",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "notyetactivated",
      None
    )

    val agentActiveEnrolment: Enrolment = Enrolment(
      "IR-SDLT-AGENT",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "activated",
      None
    )
    val agentInActiveEnrolment: Enrolment = Enrolment(
      "IR-SDLT-AGENT",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "inactivated",
      None
    )

    val agentNotYetActiveEnrolment: Enrolment = Enrolment(
      "IR-SDLT-AGENT",
      Seq(
        EnrolmentIdentifier("STORN", testStorn)
      ),
      "NotYetActivated",
      None
    )

    val emptyEnrolment: Enrolments = Enrolments(Set.empty)

    type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole]

  }
  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "the user is logged in as an AGENT" - {
      "must allow into the service: activated enrollment" - {
        "when the user has active IR-SDLT-AGENT enrolment with the correct activated identifiers" in new Fixture {
          val enrollments: Enrolments = Enrolments(Set(agentActiveEnrolment))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(Some(id) ~ enrollments ~ Some(Agent) ~ Some(User))
            )
          running(application){
            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller = new Harness(authAction)

            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
          }
        }
      }
      "must allow into the service: NotYetActivated enrollment" - {
        "when the user has active IR-SDLT-AGENT enrolment " in new Fixture {
          val enrollments: Enrolments = Enrolments(Set(agentNotYetActiveEnrolment))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(Some(id) ~ enrollments ~ Some(Agent) ~ Some(User))
            )
          running(application) {
            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller = new Harness(authAction)

            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
          }
        }
      }
      "must redirect to access denied screen " - {
        "when the user has inactive IR-SDLT-AGENT enrolment " in new Fixture {
          val enrollments: Enrolments = Enrolments(Set(agentInActiveEnrolment))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(Some(id) ~ enrollments ~ Some(Agent) ~ Some(User))
            )
          running(application) {
            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller = new Harness(authAction)

            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(
              result
            ).value mustBe controllers.routes.AccessDeniedController
              .onPageLoad()
              .url
          }
        }

      }
    }

    "the user is logged in as an ORGANISATION" - {
      "must allow into the service: with Activated enrollment" - {
        "when the user has active IR-SLDT-ORG enrolment with the correct activated identifiers" in new Fixture {
          val enrollments: Enrolments = Enrolments(Set(orgActiveEnrolment))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(Some(id) ~ enrollments ~ Some(Organisation) ~ Some(User))
            )
          running(application) {
            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller = new Harness(authAction)

            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
          }
        }
      }

      "must allow into the service: with NotYetActive enrollment" - {
        "when the user has active IR-SLDT-ORG enrolment with the correct activated identifiers" in new Fixture {
          val enrollments: Enrolments = Enrolments(Set(orgNotYetActivatedEnrolment))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(Some(id) ~ enrollments ~ Some(Organisation) ~ Some(User))
            )
          running(application) {
            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller = new Harness(authAction)

            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe OK
          }
        }
      }

      "must redirect to access denied screen"- {
        "when the user has inactive IR-SDLT-AGENT enrolment with the correct activated identifiers" in new Fixture {
          val enrollments: Enrolments = Enrolments(Set(orgInActiveEnrolment))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(Some(id) ~ enrollments ~ Some(Organisation) ~ Some(User))
            )
          running(application) {
            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller = new Harness(authAction)

            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(
              result
            ).value mustBe controllers.routes.AccessDeniedController
              .onPageLoad()
              .url
          }
        }
      }
    }

    "the user is logged in as an INDIVIDUAL" -{
      "must redirect to Unauthorised access screen" in new Fixture {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(Some(id) ~ emptyEnrolment ~ Some(Individual) ~ Some(User))
          )
        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(
            result
          ).value mustBe controllers.routes.UnauthorisedIndividualAffinityController
            .onPageLoad()
            .url
        }
      }
    }

    "the user is logged in as an ORGANISATION assistant" -{
      "must redirect to wrong role screen" in new Fixture {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(Some(id) ~ emptyEnrolment ~ Some(Organisation) ~ Some(Assistant))
          )
        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(
            result
          ).value mustBe controllers.manageAgents.routes.UnauthorisedOrganisationAffinityController
            .onPageLoad()
            .url
        }
      }
    }

    "Unable to retrieve internal id or affinity group" - {
      "fail and redirect to Unauthorised access screen" in new Fixture {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(None ~ emptyEnrolment ~ None ~ None)
          )
        running(application) {
          val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)

          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(
            result
          ).value mustBe controllers.routes.AccessDeniedController
            .onPageLoad()
            .url
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
