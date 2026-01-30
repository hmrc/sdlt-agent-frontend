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

import forms.behaviours.OptionFieldBehaviours
import forms.behaviours.StringFieldBehaviours
import models.manageAgents.ConfirmAgentContactDetails
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import utils.manageAgents.AgentDetailsTestUtil

class ConfirmAgentContactDetailsFormProviderSpec
    extends OptionFieldBehaviours
    with StringFieldBehaviours
    with AgentDetailsTestUtil
    with GuiceOneAppPerSuite {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(
    play.api.i18n.Lang.defaultLang,
    app.injector.instanceOf[play.api.i18n.MessagesApi]
  )

  val formProvider = new ConfirmAgentContactDetailsFormProvider
  val agentName = "Harborview Estates"

  val form = formProvider(agentName)
  ".value" - {

    val fieldName = "value"
    val requiredKey = messages(
      "manageAgents.confirmAgentContactDetails.error.required",
      agentName
    )

    behave like optionsField[ConfirmAgentContactDetails](
      form,
      fieldName,
      validValues = ConfirmAgentContactDetails.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
