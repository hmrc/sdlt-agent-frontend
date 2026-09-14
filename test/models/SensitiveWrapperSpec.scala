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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, SymmetricCryptoFactory}

class SensitiveWrapperSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private implicit val crypto: Encrypter with Decrypter =
    SymmetricCryptoFactory.aesGcmCrypto("z9WMSuFsHqfY5F2wgIcEvcnwyRTRB4dyPWfMbCbCXfM=")

  private val json = Json.obj(
    "nationalInsuranceNumber" -> "AA123456A",
    "nested" -> Json.obj("surname" -> "Smith")
  )

  "SensitiveWrapper" - {

    "case class" - {

      "must expose the value it was constructed with" in {
        SensitiveWrapper("secret").decryptedValue mustBe "secret"
      }

      "must support equality on the underlying value" in {
        SensitiveWrapper(json) mustEqual SensitiveWrapper(json)
      }

      "must not be equal when the underlying values differ" in {
        SensitiveWrapper(json) must not equal SensitiveWrapper(Json.obj())
      }
    }

    ".writes" - {

      "must be found implicitly for any type with a Writes" in {
        implicitly[Writes[SensitiveWrapper[JsObject]]]
        implicitly[Writes[SensitiveWrapper[String]]]
      }

      "must write the value as a JsString" in {
        Json.toJson(SensitiveWrapper(json)) mustBe a[JsString]
      }

      "must not leak the plaintext into the output" in {
        val result = Json.toJson(SensitiveWrapper(json)).toString

        result must not include "AA123456A"
        result must not include "Smith"
        result must not include "nationalInsuranceNumber"
      }

      "must produce output that decrypts back to the original JSON" in {
        val ciphertext = Json.toJson(SensitiveWrapper(json)).as[String]
        val plaintext = crypto.decrypt(Crypted(ciphertext)).value

        Json.parse(plaintext) mustEqual json
      }

      "must produce different ciphertext each time for the same value" in {
        val first = Json.toJson(SensitiveWrapper(json)).as[String]
        val second = Json.toJson(SensitiveWrapper(json)).as[String]

        first must not equal second
      }
    }

    ".reads" - {

      "must be found implicitly for any type with a Reads" in {
        implicitly[Reads[SensitiveWrapper[JsObject]]]
        implicitly[Reads[SensitiveWrapper[String]]]
      }

      "must decrypt a value written by the corresponding writes" in {
        val written = Json.toJson(SensitiveWrapper(json))
        val result = Json.fromJson[SensitiveWrapper[JsObject]](written).asEither.value

        result.decryptedValue mustEqual json
      }

      "must fail when the value is not valid ciphertext" in {
        assertThrows[Exception] {
          Json.fromJson[SensitiveWrapper[JsObject]](JsString("not-encrypted"))
        }
      }

      "must fail when the value is not a JsString" in {
        val result = Json.fromJson[SensitiveWrapper[JsObject]](json).asEither

        result.isLeft mustBe true
      }
    }

    "round trip" - {

      "must preserve a JsObject" in {
        val written = Json.toJson(SensitiveWrapper(json))

        Json.fromJson[SensitiveWrapper[JsObject]](written).asEither.value.decryptedValue mustEqual json
      }

      "must preserve a String" in {
        val written = Json.toJson(SensitiveWrapper("some sensitive text"))

        Json.fromJson[SensitiveWrapper[String]](written).asEither.value.decryptedValue mustBe "some sensitive text"
      }

      "must preserve an empty JsObject" in {
        val written = Json.toJson(SensitiveWrapper(Json.obj()))

        Json.fromJson[SensitiveWrapper[JsObject]](written).asEither.value.decryptedValue mustEqual Json.obj()
      }

      "must preserve a deeply nested JsObject" in {
        val nested = Json.obj(
          "level1" -> Json.obj(
            "level2" -> Json.obj(
              "level3" -> Json.arr("a", "b", "c"),
              "number" -> 42
            )
          )
        )

        val written = Json.toJson(SensitiveWrapper(nested))

        Json.fromJson[SensitiveWrapper[JsObject]](written).asEither.value.decryptedValue mustEqual nested
      }

      "must preserve non-ASCII characters" in {
        val value = Json.obj("name" -> "café £100")
        val written = Json.toJson(SensitiveWrapper(value))

        Json.fromJson[SensitiveWrapper[JsObject]](written).asEither.value.decryptedValue mustEqual value
      }
    }
  }
}