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

package forms.manageAgents

import forms.behaviours.{BooleanFieldBehaviours, OptionFieldBehaviours, StringFieldBehaviours}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import utils.manageAgents.AgentDetailsTestUtil

class ConfirmAgentContactDetailsFormProviderSpec extends BooleanFieldBehaviours {

  private implicit val messages: Messages = stubMessages()

  val requiredKey = "manageAgents.confirmAgentContactDetails.error.required"
  val invalidKey = "error.boolean"
  val agentName = "Harborview Estates"

  val formProvider = new ConfirmAgentContactDetailsFormProvider()

  val form = formProvider(agentName)
 
  ".value" - {

    val fieldName = "value"

    "must bind true and false values correctly" in {
      form.bind(Map(fieldName -> "true")).get mustBe true
      form.bind(Map(fieldName -> "false")).get mustBe false
    }

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
