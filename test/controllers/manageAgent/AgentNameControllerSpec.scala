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

package controllers.manageAgent

import base.SpecBase
import controllers.routes
import forms.manageAgents.AgentNameFormProvider
import models.{AgentDetails, CheckMode, NormalMode}
import models.manageAgents.RemoveAgent
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.StampDutyLandTaxService
import views.html.manageAgents.RemoveAgentView

import scala.concurrent.Future

class AgentNameControllerSpec extends SpecBase with MockitoSugar {

  val storn: String = "STN001"

  val testAgentDetails: AgentDetails = AgentDetails(
    storn = "STN001",
    agentReferenceNumber = Some("ARN001"),
    name = "Harborview Estates",
    houseNumber = "22A",
    addressLine1 = "Queensway",
    addressLine2 = None,
    addressLine3 = "Birmingham",
    addressLine4 = None,
    postcode = Some("B2 4ND"),
    phoneNumber = "01214567890",
    emailAddress = "info@harborviewestates.co.uk",
    agentId = "AGT001",
    isAuthorised = 1
  )

  lazy val NormalAgentNameRequestRoute: String = controllers.manageAgents.routes.AgentNameController.onPageLoad(NormalMode, storn).url
  lazy val CheckAgentNameRequestRoute: String = controllers.manageAgents.routes.AgentNameController.onPageLoad(CheckMode, storn).url

  val formProvider = new AgentNameFormProvider()
  val form = formProvider()

  val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]

  def onwardRoute: Call = controllers.routes.HomeController.onPageLoad()

  "AgentNameController" - {
    "must return OK and the correct view for a GET" in {
      "Normal Mode" {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, NormalAgentNameRequestRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ConfirmEmailAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }
    }
  }
}
