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
import play.api.i18n.Messages

import javax.inject.Inject

class AgentContactDetailsFormProvider @Inject() extends Mappings {

  private val phoneInvalidRegex = "^[0-9+\\-\\s()]+$"
  private val phoneInvalidFormatRegex = "^[0-9+\\-\\s()]+$"
  private val emailInvalidFormatRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  private val emailInvalidRegex = "^[A-Za-z0-9&'@\\/.\\-? ]+$"
  private val maxAgentPhoneLength = 14
  private val maxAgentEmailLength = 36

  def apply(agentName: String)(implicit messages: Messages): Form[AgentContactDetails] =

    Form(
      mapping(
        "phone" -> text(messages("manageAgents.agentContactDetails.error.phoneRequired", agentName))
          .verifying(maxLength(maxAgentPhoneLength, messages("manageAgents.agentContactDetails.error.phoneLength", agentName)))
            .verifying(regexp(phoneInvalidRegex, messages("manageAgents.agentContactDetails.error.phoneInvalid", agentName)))
            .verifying(regexp(phoneInvalidFormatRegex, "manageAgents.agentContactDetails.error.phoneInvalidFormat")),

        "email" -> text(messages("manageAgents.agentContactDetails.error.emailRequired", agentName))
          .verifying(maxLength(maxAgentEmailLength, messages("manageAgents.agentContactDetails.error.emailLength", agentName)))
          .verifying(regexp(emailInvalidFormatRegex, messages("manageAgents.agentContactDetails.error.emailInvalidFormat", agentName)))
          .verifying(regexp(emailInvalidRegex, messages("manageAgents.agentContactDetails.error.emailInvalid", agentName))),
      )(AgentContactDetails.apply)(contactDetails => Some((contactDetails.phone, contactDetails.email)))
    )
}
