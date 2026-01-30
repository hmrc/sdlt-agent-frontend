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

package models.manageAgents

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class RemoveAgentSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues {

  "RemoveAgent" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(RemoveAgent.values)

      forAll(gen) { removeAgent =>
        JsString(removeAgent.toString)
          .validate[RemoveAgent]
          .asOpt
          .value mustEqual removeAgent
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!RemoveAgent.values
        .map(_.toString)
        .contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[RemoveAgent] mustEqual JsError(
          "error.invalid"
        )
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(RemoveAgent.values)

      forAll(gen) { removeAgent =>
        Json.toJson(removeAgent) mustEqual JsString(removeAgent.toString)
      }
    }
    "options" - {

      "must build two radio items in order (yes, no) with correct labels, values and ids" in {

        implicit val messages: Messages = stubMessages()

        val radios = RemoveAgent.options

        radios mustEqual Seq(
          RadioItem(
            content = Text(messages("site.yes")),
            value = Some("true"),
            id = Some("value_0")
          ),
          RadioItem(
            content = Text(messages("site.no")),
            value = Some("false"),
            id = Some("value_1")
          )
        )
      }

      "must derive ids from index positions" in {

        implicit val messages: Messages = stubMessages()

        val radios = RemoveAgent.options

        radios.map(_.id) mustEqual Seq(Some("value_0"), Some("value_1"))
      }

      "must map values to the underlying WithName strings" in {

        implicit val messages: Messages = stubMessages()

        val radios = RemoveAgent.options

        radios.map(_.value) mustEqual Seq(
          Some(RemoveAgent.Option1.toString),
          Some(RemoveAgent.Option2.toString)
        )
      }
    }
  }
}
