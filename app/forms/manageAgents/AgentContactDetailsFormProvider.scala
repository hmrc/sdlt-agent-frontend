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

import forms.constraints.{OptionalEmailFormat, OptionalMaxLength}
import models.manageAgents.AgentContactDetails
import play.api.data.Form
import forms.mappings.Mappings
import play.api.data.Forms.mapping
import play.api.data.validation.Constraint

import javax.inject.Inject

class AgentContactDetailsFormProvider @Inject() extends Mappings{

  private val phoneRegex = "^[A-Za-z0-9&'@/.\\-? ]+$"
  private val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  private val maxAgentPhoneLength = 14
  private val maxAgentEmailLength = 36

  def apply(): Form[AgentContactDetails] =
    Form(
      mapping(
        "phone" -> text("manageAgents.agentContactDetails.error.phoneRequired")
          .verifying(maxLength(maxAgentPhoneLength, "manageAgents.agentContactDetails.error.phoneLength"))
          .verifying(regexp(phoneRegex, "manageAgents.agentContactDetails.error.phoneInvalid")),
        "email" -> text("manageAgents.agentContactDetails.error.emailRequired")
          .verifying(maxLength(maxAgentEmailLength, "manageAgents.agentContactDetails.error.emailLength"))
          .verifying(regexp(emailRegex,"manageAgents.agentContactDetails.error.emailInvalidFormat"))
      )(AgentContactDetails.apply)(contactDetails => Some((contactDetails.phone, contactDetails.email)))
    )
}
