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

package forms.constraints

import play.api.data.validation._

object OptionalMaxLength {
  def apply(max: Int, errorKey: String): Constraint[Option[String]] =
    Constraint("constraints.maxLength") {
      case Some(value) if value.length > max =>
        Invalid(ValidationError(errorKey, max))
      case _ => Valid
    }
}

object OptionalEmailFormat {
  def apply(errorKey: String): Constraint[Option[String]] =
    Constraint("constraints.email") {
      case Some(value) if !value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") =>
        Invalid(ValidationError(errorKey))
      case _ => Valid
    }
}