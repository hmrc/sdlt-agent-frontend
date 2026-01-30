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

import models.manageAgents.AgentContactDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.i18n.Messages

import javax.inject.Inject

class AgentContactDetailsFormProvider @Inject() {

  private val phoneInvalidRegex = "^[0-9+\\s\\-()]+$"
  private val phoneInvalidFormatRegex = "^[0-9+\\s\\-()]+$"
  private val emailInvalidFormatRegex =
    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  private val emailInvalidRegex = "^[A-Za-z0-9&'@\\/.\\-? ]+$"
  private val maxAgentPhoneLength = 14
  private val maxAgentEmailLength = 36

  def apply(
      agentName: String
  )(implicit messages: Messages): Form[AgentContactDetails] =

    Form(
      mapping(
        "phone" -> optional(text)
          .transform[Option[String]](_.map(_.trim), identity)
          .verifying(
            messages(
              "manageAgents.agentContactDetails.error.phoneInvalid",
              agentName
            ),
            _.forall(_.matches(phoneInvalidRegex))
          )
          .verifying(
            messages(
              "manageAgents.agentContactDetails.error.phoneInvalidFormat",
              agentName
            ),
            _.forall(_.matches(phoneInvalidFormatRegex))
          )
          .verifying(
            messages(
              "manageAgents.agentContactDetails.error.phoneLength",
              agentName
            ),
            _.forall(phone =>
              phone.replaceAll("\\s", "").length <= maxAgentPhoneLength
            )
          ),
        "email" -> optional(text)
          .verifying(
            messages(
              "manageAgents.agentContactDetails.error.emailLength",
              agentName
            ),
            _.forall(_.length <= maxAgentEmailLength)
          )
          .verifying(
            messages(
              "manageAgents.agentContactDetails.error.emailInvalidFormat",
              agentName
            ),
            _.forall(_.matches(emailInvalidFormatRegex))
          )
          .verifying(
            messages(
              "manageAgents.agentContactDetails.error.emailInvalid",
              agentName
            ),
            _.forall(_.matches(emailInvalidRegex))
          )
      )(AgentContactDetails.apply)(contactDetails =>
        Some((contactDetails.phone, contactDetails.email))
      )
    )
}
