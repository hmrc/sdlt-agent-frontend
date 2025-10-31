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

import forms.behaviours.StringFieldBehaviours
import forms.mappings.manageAgents.AgentContactDetailsFormProvider
import play.api.data.{Form, FormError}

class AgentContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new AgentContactDetailsFormProvider()()

  ".phone" - {

    val fieldName = "phone"
    val lengthKey = "manageAgents.agentContactDetails.error.phoneLength"
    val maxLength = 14

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
    
  }

  ".email" - {

    val fieldName = "email"
    val lengthKey = "manageAgents.agentContactDetails.error.emailLength"
    val invalidKey = "manageAgents.agentContactDetails.error.emailInvalid"
    val maxLength = 36

    behave like emailLengthValidation(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like invalidField(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidKey),
      "test"
    )
  }

}
