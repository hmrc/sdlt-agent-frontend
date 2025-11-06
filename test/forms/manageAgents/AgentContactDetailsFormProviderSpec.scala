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
import play.api.data.FormError
import utils.mangeAgents.AgentDetailsTestUtil

class AgentContactDetailsFormProviderSpec extends StringFieldBehaviours with AgentDetailsTestUtil {

  val form = new AgentContactDetailsFormProvider()(testAgentDetails)

  ".phone" - {

    val fieldName = "phone"
    val lengthKey = "manageAgents.agentContactDetails.error.phoneLength"
    val invalidKey = "manageAgents.agentContactDetails.error.phoneInvalid"
    val maxLength = 14

    behave like lengthValidation(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
    )

    behave like invalidField(
      form,
      fieldName,
      requiredError = FormError(fieldName, invalidKey),
      "0123456789>"
    )
    
  }

  ".email" - {

    val fieldName = "email"
    val lengthKey = "manageAgents.agentContactDetails.error.emailLength"
    val invalidKey = "manageAgents.agentContactDetails.error.emailInvalid"
    val maxLength = 36

    behave like lengthValidation(
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
