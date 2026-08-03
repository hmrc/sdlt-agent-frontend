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
import models.manageAgents.AgentContactDetails
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages

class AgentContactDetailsFormProviderSpec
  extends StringFieldBehaviours
    with GuiceOneAppPerSuite {

  implicit val messages: Messages = stubMessages()

  val agentName = "Agent Name"
  val formProvider = new AgentContactDetailsFormProvider()
  val form: Form[AgentContactDetails] = formProvider(agentName)


  "AgentContactDetailsFormProvider" - {

    ".phone" - {

      val fieldName = "phone"
      val lengthKey = "manageAgents.agentContactDetails.error.phoneLength"
      val invalidKey = "manageAgents.agentContactDetails.error.phoneInvalid"
      val maxLength = 14

      "must bind valid phone number form data" in {
        val validNumbers = Seq(
          "1234567890",
          "12345678912345",
          "1",
          "ABC123",
          "A1B2C3!",
          "Test_Value",
          "Hello.World+!@",
          "A+B=C",
          "Value:100%",
          "OK;GO"
        )

        validNumbers.foreach { number =>
          val result = form.bind(
            Map(
              fieldName -> number,
              "email" -> "test@example.com"
            )
          )

          result.errors mustBe empty
          result.get.phone mustBe Some(number)
        }
      }

      "must strip spaces, hyphens and brackets before storing" in {
        val inputs = Seq(
          "9876-5432" -> "98765432",
          "(Agent)42" -> "Agent42",
          "+44 808 157 0192" -> "+448081570192"
        )
        inputs.foreach { case (input, expected) =>
          val result = form.bind(Map(fieldName -> input, "email" -> "test@example.com"))
          result.errors mustBe empty
          result.get.phone mustBe Some(expected)
        }
      }

      "must allow a phone number that exceeds 14 characters raw but is within limit after stripping" in {
        val longWithSpaces = "+44 808 157 0192"
        val result = form.bind(Map(fieldName -> longWithSpaces, "email" -> "test@example.com"))
        result.errors mustBe empty
        result.get.phone mustBe Some("+448081570192")
      }

      "must bind empty strings as None" in {
        val result = form.bind(
          Map(
            fieldName -> "",
            "email" -> "test@example.com"
          )
        )
        result.errors mustBe empty
        result.get.phone mustBe None
      }

      "must bind when field is missing" in {
        val result = form.bind(
          Map(
            "email" -> "test@example.com"
          )
        )
        result.errors mustBe empty
        result.get.phone mustBe None
      }

      "must not bind strings longer than 14 characters" in {
        val longNumber = "1" * (maxLength + 1)
        val result = form.bind(
          Map(
            fieldName -> longNumber,
            "email" -> "test@example.com"
          )
        )
        result.errors must contain(FormError(fieldName, lengthKey))
      }

      "must not bind invalid phone number values" in {
        val invalidNumbers = Seq(
          "123456789#",
          "123$4567",
          "12€34",
          "abc©def",
          "987~^`",
          "phone🙂",
          "num>value",
          "num<value",
          "hello|world",
          "test\"",
          "back\\slash"
        )

        invalidNumbers.foreach { number =>
          val result = form.bind(
            Map(
              fieldName -> number,
              "email" -> "test@example.com"
            )
          )
          result.errors must contain(
            FormError(fieldName, invalidKey)
          )
        }
      }
    }

    ".email" - {

      val fieldName = "email"
      val lengthKey = "manageAgents.agentContactDetails.error.maxEmailLength"
      val invalidKey = "manageAgents.agentContactDetails.error.emailInvalid"
      val invalidFormatKey = "manageAgents.agentContactDetails.error.emailInvalidFormat"

      val maxLength = 36

      "must bind valid email address form data" in {
        val validEmails = Seq(
          "test@example.com",
          "user.name@domain.co.uk",
          "hello+world@sub.domain.com",
          "simple123@numbers.net",
          "UPPERCASE@EXAMPLE.COM",
          "name_with_underscores@domain.org",
          "dots.in.name@domain.io",
          "hyphen-name@domain-name.com",
          "a@b.com"
        )

        validEmails.foreach { email =>
          val result = form.bind(
            Map(
              fieldName -> email,
              "phone" -> "01234567890"
            )
          )
          result.errors mustBe empty
          result.get.email mustBe Some(email)
        }
      }

      "must bind empty strings as None" in {
        val result = form.bind(
          Map(
            fieldName -> "",
            "phone" -> "01234567890"
          )
        )
        result.errors mustBe empty
        result.get.email mustBe None
      }

      "must bind when field is missing" in {
        val result = form.bind(
          Map(
            "phone" -> "01234567890"
          )
        )
        result.errors mustBe empty
        result.get.email mustBe None
      }

      "must not bind strings longer than 36 characters" in {
        val longEmail = ("a" * maxLength) + "@test.com"
        val result = form.bind(
          Map(
            fieldName -> longEmail,
            "phone" -> "01234567890"
          )
        )
        result.errors must contain(FormError(fieldName, lengthKey))
      }

      "must not bind email address values with invalid characters" in {
        val invalidCharEmails = Seq(
          "user@domain|com",
          "hello@domain>.com",
          "name@domain<.com",
          "quote\"@domain.com",
          "single'quote@domain.com",
          "`backtick`@domain.com"
        )

        invalidCharEmails.foreach { email =>
          val result = form.bind(
            Map(
              fieldName -> email,
              "phone" -> "123456789"
            )
          )
          result.errors must contain(
            FormError(fieldName, invalidKey)
          )
        }
      }

      "must not bind email address values with invalid format" in {
        val invalidFormatEmails = Seq(
          "test@@example.com",
          "te@st@example.com",
          "@missinglocal.com",
          "missingdomain@"
        )

        invalidFormatEmails.foreach { email =>
          val result = form.bind(
            Map(
              fieldName -> email,
              "phone" -> "123456789"
            )
          )
          result.errors must contain(
            FormError(fieldName, invalidFormatKey)
          )
        }
      }

      "oneRequired validation" - {

        val phoneOrEmailRequiredKey = "manageAgents.agentContactDetails.error.phoneOrEmailRequired"

        "must fail when both phone and email are empty" in {
          val result = form.bind(Map("phone" -> "", "email" -> ""))
          result.errors must contain(FormError("", phoneOrEmailRequiredKey))
        }

        "must fail when both phone and email are missing" in {
          val result = form.bind(Map.empty[String, String])
          result.errors must contain(FormError("", phoneOrEmailRequiredKey))
        }
      }
    }
  }
}
