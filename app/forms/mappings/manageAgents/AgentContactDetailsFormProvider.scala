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

package forms.mappings.manageAgents

import forms.constraints.{OptionalEmailFormat, OptionalMaxLength}
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.Constraint

class AgentContactDetailsFormProvider {

  private val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  
  def apply(): Form[(Option[String], Option[String])] = {
    Form(
      mapping(
        "phone" -> optional(text)
          .verifying(OptionalMaxLength(14, "manageAgents.agentContactDetails.error.phoneLength")),
        "email" -> optional(text)
          .verifying(OptionalMaxLength(36, "manageAgents.agentContactDetails.error.emailLength"))
          .verifying(OptionalEmailFormat("manageAgents.agentContactDetails.error.emailInvalid"))
      )(Tuple2.apply)(Tuple2.unapply)
    )
  }
}
