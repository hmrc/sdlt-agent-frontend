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

package views

import forms.manageAgents.AgentContactDetailsFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.StampDutyLandTaxService
import views.html.manageAgents.AgentContactDetailsView
import utils.mangeAgents.AgentDetailsTestUtil

import scala.concurrent.Future

class AgentContactDetailsViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with AgentDetailsTestUtil {

  lazy val AgentContactDetailsRoute = controllers.manageAgents.routes.AgentContactDetailsController.onPageLoad(NormalMode, "").url

  trait Setup {
    private val agentReferenceNumber: String = "ARN001"
    val formProvider = new AgentContactDetailsFormProvider()
    val form: Form[?] = formProvider(testAgentDetails)
    val postAction = controllers.manageAgents.routes.AgentContactDetailsController.onSubmit(NormalMode, agentReferenceNumber)
    val service: StampDutyLandTaxService = mock[StampDutyLandTaxService]
    implicit val request: Request[?] = FakeRequest()
    implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])

    val view: AgentContactDetailsView = app.injector.instanceOf[AgentContactDetailsView]


    when(service.getAgentDetails(any(), any())(any()))
      .thenReturn(Future.successful(Some(testAgentDetails)))
  }

  "AgentContactDetailsView" should {
    "render the page with title and heading" in new Setup {
      val html = view(form, NormalMode, postAction, testAgentDetails)
      val doc = org.jsoup.Jsoup.parse(html.toString())

      doc.select("span.govuk-caption-xl").text() mustBe messages("manageAgents.agentContactDetails.caption", testAgentDetails.agentName)
      doc.select("h1").text() mustBe messages("manageAgents.agentContactDetails.heading", testAgentDetails.agentName)
    }
  }

  "display error messages when form has errors" in new Setup {
    val errorForm = form
      .withError("phone", "manageAgents.agentContactDetails.error.phoneLength", testAgentDetails.agentName)
      .withError("email", "manageAgents.agentContactDetails.error.emailLength", testAgentDetails.agentName)
      .withError("email", "manageAgents.agentContactDetails.error.emailInvalid", testAgentDetails.agentName)

    val html = view(errorForm, NormalMode, postAction, testAgentDetails)
    val doc = org.jsoup.Jsoup.parse(html.toString())
    val errorSummaryText = doc.select(".govuk-error-summary").text()

    errorSummaryText must include(messages("manageAgents.agentContactDetails.error.phoneLength", testAgentDetails.agentName))
    errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailLength", testAgentDetails.agentName))
    errorSummaryText must include(messages("manageAgents.agentContactDetails.error.emailInvalid", testAgentDetails.agentName))
  }
}

