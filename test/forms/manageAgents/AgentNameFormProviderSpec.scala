/*
 * Copyright 2026 HM Revenue & Customs
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
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.data.Form
import play.api.i18n.Messages

class AgentNameFormProviderSpec
    extends StringFieldBehaviours
    with GuiceOneServerPerSuite {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(
    play.api.i18n.Lang.defaultLang,
    app.injector.instanceOf[play.api.i18n.MessagesApi]
  )

  val form: Form[String] = new AgentNameFormProvider()()

  "AgentNameFormProvider" - {

    "must bind valid agent name" in {
      val result = form.bind(Map("value" -> "Agent Name"))

      result.errors mustBe empty
      result.value mustBe Some("Agent Name")
    }
    "must bind valid agentName with all the special characters " in {
      val result = form.bind(Map("value" -> "Agent, Name12345&@/.-?"))

      result.errors mustBe empty
      result.value mustBe Some("Agent, Name12345&@/.-?")
    }

    "must reject invalid characters" in {
      val result = form.bind(Map("value" -> "Agent#"))
      result.errors.map(_.message) must contain(
        "manageAgents.agentName.error.invalid"
      )
    }

    "must reject empty value " in {
      val result = form.bind(Map("value" -> ""))
      result.errors.map(_.message) must contain(
        "manageAgents.agentName.error.required"
      )
    }

    "must reject name longer than 28 characters" in {
      val result =
        form.bind(Map("value" -> "International Agency Services Ltd 123456"))
      result.errors.map(_.message) must contain(
        "manageAgents.agentName.error.length"
      )
    }
  }

}
