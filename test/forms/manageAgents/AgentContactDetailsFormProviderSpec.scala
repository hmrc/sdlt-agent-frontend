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

//import forms.behaviours.StringFieldBehaviours
//import play.api.data.FormError
//
//class AgentContactDetailsFormProviderSpec extends StringFieldBehaviours {
//
//  val form = new AgentContactDetailsFormProvider
//
//  ".phone" - {
//
//    val fieldName = "phone"
//    val lengthKey = "manageAgents.agentContactDetails.error.phoneLength"
//    val maxLength = 14
//
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//  }
//
//  ".email" - {
//
//    val fieldName = "email"
//    val lengthKey = "manageAgents.agentContactDetails.error.emailLength"
//    val invalidKey = "manageAgents.agentContactDetails.error.emailInvalid"
//    val maxLength = 36
//
//    behave like lengthValidation(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//    behave like invalidField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, invalidKey),
//      "test"
//    )
//  }
//
//}

//import forms.behaviours.{OptionFieldBehaviours, StringFieldBehaviours}
//import models.manageAgents.AgentContactDetails
//import org.scalacheck.Gen
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.data.FormError
//import play.api.i18n.{Messages, MessagesApi}
//import play.api.test.FakeRequest
//
//class AgentContactDetailsFormProviderSpec extends OptionFieldBehaviours with StringFieldBehaviours with GuiceOneAppPerSuite{
//
//  implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])
//
//  val formProvider = new AgentContactDetailsFormProvider
//  val agentName = "Agent Name"
//  val form = formProvider(agentName)
//
//  ".phone" - {
//
//    val fieldName = "phone"
//    val lengthKey = messages("manageAgents.agentContactDetails.error.phoneLength", agentName)
//    val formatKey = messages("manageAgents.agentContactDetails.error.phoneInvalidFormat", agentName)
//    val charKey = messages("manageAgents.agentContactDetails.error.phoneInvalid", agentName)
//    val missing = messages("manageAgents.agentContactDetails.error.phoneRequired", agentName)
//    val validPhones: Gen[String] = Gen.oneOf(
//      "01632 960 001",
//      "07700 900 982"
//    )
//    val maxLength = 14
//
//    "should return error message when empty" - {
//      behave like mandatoryField(
//        form,
//        fieldName,
//        requiredError = FormError(fieldName, missing)
//      )
//    }
//
//      behave like
//        fieldThatBindsValidData(
//          form,
//          fieldName,
//          validPhones
//        )
//    "should return error message when length is too long" - {
//      behave like fieldWithMaxLength(
//            form,
//            fieldName,
//            maxLength = maxLength,
//            lengthError = FormError(fieldName, (lengthKey,charKey, formatKey ).toString())
//      )
//    }
//    "should return error message when phone has invalid characters" - {
//      behave like invalidField(
//        form,
//        fieldName,
//        requiredError = FormError(fieldName, charKey),
//        "test"
//      )
//    }
//    "should return error message when phone has invalid format" - {
//      behave like invalidField(
//        form,
//        fieldName,
//        requiredError = (FormError(fieldName, charKey)),
//        "test"
//      )
//    }
//  }
//
//  ".email" - {
//
//    val fieldName = "email"
//    val lengthKey = messages("manageAgents.agentContactDetails.error.emailLength", agentName)
//    val invalidKey = messages("manageAgents.agentContactDetails.error.emailInvalid", agentName)
//    val maxLength = 36
//
//    behave like lengthValidation(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey)
//    )
//
//    behave like invalidField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, invalidKey),
//      "test"
//    )
//  }
//}

//import forms.behaviours.{IntFieldBehaviours, StringFieldBehaviours}
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.data.FormError
//import play.api.i18n.{Messages, MessagesApi}
//import org.scalacheck.Gen
//
//class AgentContactDetailsFormProviderSpec
//  extends StringFieldBehaviours
//    with IntFieldBehaviours
//    with GuiceOneAppPerSuite {
//
//  implicit val messages: Messages =
//    play.api.i18n.MessagesImpl(
//      play.api.i18n.Lang.defaultLang,
//      app.injector.instanceOf[MessagesApi]
//    )
//
//  val formProvider = new AgentContactDetailsFormProvider
//  val agentName = "Agent Name"
//  val form = formProvider(agentName)
//
//  ".phone" - {
//
//    val fieldName = "phone"
//    val maxLength = 14
//    val validPhones: Gen[String] = Gen.oneOf(
//      "01632 960 001",
//      "07700 900 982"
//    )
//
//    "should allow valid submission" - {
//      behave like fieldThatBindsValidData(
//        form,
//        fieldName,
//        validPhones
//      )
//    }
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, messages("manageAgents.agentContactDetails.error.phoneLength", agentName), Seq(maxLength))
//    )
//
//  }
//
//
//  ".email" - {
//
//    val fieldName = "email"
//    val maxLength = 36
//    val validEmails: Gen[String] = Gen.oneOf(
//      "agent@example.com",
//      "john.o'connor@agency.co.uk",
//      "team-42@agents.org"
//    )
//
//    "should allow valid submission" - {
//      behave like fieldThatBindsValidData(
//        form,
//        fieldName,
//        validEmails
//      )
//    }
//
//      behave like lengthValidation(
//        form,
//        fieldName,
//        maxLength,
//        lengthError = FormError(
//          fieldName,
//          messages("manageAgents.agentContactDetails.error.emailLength", agentName)
//        )
//      )
//
//  }
//
//}
import forms.behaviours.StringFieldBehaviours
import models.manageAgents.AgentContactDetails
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.test.Helpers._

class AgentContactDetailsFormProviderSpec
  extends StringFieldBehaviours
    with GuiceOneAppPerSuite {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(play.api.i18n.Lang.defaultLang, app.injector.instanceOf[play.api.i18n.MessagesApi])

  val agentName = "Agent Name"
  val formProvider = new AgentContactDetailsFormProvider()
  val form: Form[AgentContactDetails] = formProvider(agentName)

  val validPhone = "01234 567890"
  val validEmail = "test@example.com"

  "AgentContactDetailsFormProvider" - {

    "must bind valid phone and email" in {
      val result = form.bind(Map("phone" -> validPhone, "email" -> validEmail))
      result.errors mustBe empty
      result.value mustBe Some(AgentContactDetails(validPhone, validEmail))
    }

    "must give required error when phone is missing" in {
      val result = form.bind(Map("phone" -> "", "email" -> validEmail))
      result.errors must contain only FormError(
        "phone",
        messages("manageAgents.agentContactDetails.error.phoneRequired", agentName)
      )
    }

    "must give required error when email is missing" in {
      val result = form.bind(Map("phone" -> validPhone, "email" -> ""))
      result.errors must contain only FormError(
        "email",
        messages("manageAgents.agentContactDetails.error.emailRequired", agentName)
      )
    }

    "must not bind phone longer than 14 characters" in {
      val result = form.bind(Map("phone" -> "0123456789012345", "email" -> validEmail))
      result.errors.exists(_.message == messages("manageAgents.agentContactDetails.error.phoneLength", agentName)) mustBe true
    }

    "must not bind invalid phone characters" in {
      val result = form.bind(Map("phone" -> "abcd1234", "email" -> validEmail))
      result.errors.exists(_.message == messages("manageAgents.agentContactDetails.error.phoneInvalid", agentName)) mustBe true
    }

    "must not bind invalid phone format" in {
      val result = form.bind(Map("phone" -> "+44@", "email" -> validEmail))
      result.errors.exists(_.message == "manageAgents.agentContactDetails.error.phoneInvalidFormat") mustBe true
    }

    "must not bind email longer than 36 characters" in {
      val longEmail = ("a" * 40) + "@example.com"
      val result = form.bind(Map("phone" -> validPhone, "email" -> longEmail))
      result.errors.exists(_.message == messages("manageAgents.agentContactDetails.error.emailLength", agentName)) mustBe true
    }

    "must not bind invalid email format" in {
      val result = form.bind(Map("phone" -> validPhone, "email" -> "not-an-email"))
      result.errors.exists(_.message == messages("manageAgents.agentContactDetails.error.emailInvalidFormat", agentName)) mustBe true
    }

    "must not bind email with invalid characters" in {
      val result = form.bind(Map("phone" -> validPhone, "email" -> "bad#chars@email.com"))
      result.errors.exists(_.message == messages("manageAgents.agentContactDetails.error.emailInvalid", agentName)) mustBe true
    }
  }
}
