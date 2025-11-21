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

import forms.behaviours.{OptionFieldBehaviours, StringFieldBehaviours}
import models.manageAgents.RemoveAgent
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import utils.mangeAgents.AgentDetailsTestUtil

class RemoveAgentFormProviderSpec extends OptionFieldBehaviours with StringFieldBehaviours with AgentDetailsTestUtil with GuiceOneAppPerSuite {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])

  val formProvider =  new RemoveAgentFormProvider


  val form = formProvider(testAgentDetails)
  ".value" - {

    val fieldName = "value"
    val requiredKey = messages("manageAgents.removeAgent.error.required", testAgentDetails.name)

    behave like optionsField[RemoveAgent](
      form,
      fieldName,
      validValues  = RemoveAgent.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
