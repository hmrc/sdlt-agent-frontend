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

package config

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Configuration
import uk.gov.hmrc.crypto.PlainText

class CryptoProviderSpec extends AnyFreeSpec with Matchers {

  private val testKey = "z9WMSuFsHqfY5F2wgIcEvcnwyRTRB4dyPWfMbCbCXfM="

  private val fake = new FakeEncrypterDecrypter()

  private def provider(config: Map[String, Any]): CryptoProvider =
    new CryptoProvider(Configuration.from(config), fake)

  private val enabled = Map(
    "mongodb.encryption.enabled" -> true,
    "mongodb.encryption.key" -> testKey
  )

  private val disabled = Map(
    "mongodb.encryption.enabled" -> false,
    "mongodb.encryption.key" -> testKey
  )

  ".get" - {

    "when encryption is enabled" - {

      "must not return the fake" in {
        provider(enabled).get() must not be theSameInstanceAs(fake)
      }

      "must produce ciphertext that differs from the plaintext" in {
        provider(enabled).get().encrypt(PlainText("AA123456A")).value must not be "AA123456A"
      }

      "must round-trip a value" in {
        val crypto = provider(enabled).get()

        crypto.decrypt(crypto.encrypt(PlainText("some sensitive text"))).value mustBe "some sensitive text"
      }

      "must produce different ciphertext for the same plaintext on each call" in {
        val crypto = provider(enabled).get()

        crypto.encrypt(PlainText("value")).value must not equal crypto.encrypt(PlainText("value")).value
      }

      "must produce instances that can decrypt each other's output" in {
        val first = provider(enabled).get()
        val second = provider(enabled).get()

        second.decrypt(first.encrypt(PlainText("shared"))).value mustBe "shared"
      }

      "must fail when the key is missing" in {
        assertThrows[Exception] {
          provider(Map("mongodb.encryption.enabled" -> true)).get()
        }
      }

      "must fail on first use when the key is not valid base64" in {
        val crypto = provider(
          Map(
            "mongodb.encryption.enabled" -> true,
            "mongodb.encryption.key" -> "ENC[GPGJSON,5c3ba28c-2cbe-4228-b6e3-bf7427eaec82]"
          )
        ).get()

        // AesGCMCrypto decodes the key lazily, so this surfaces on encrypt rather than construction
        assertThrows[IllegalArgumentException] {
          crypto.encrypt(PlainText("value"))
        }
      }

      "must fail on first use when the key is the wrong length" in {
        val crypto = provider(
          Map(
            "mongodb.encryption.enabled" -> true,
            "mongodb.encryption.key" -> "dG9vc2hvcnQ="
          )
        ).get()

        assertThrows[Exception] {
          crypto.encrypt(PlainText("value"))
        }
      }
    }

    "when encryption is disabled" - {

      "must return the injected fake" in {
        provider(disabled).get() mustBe theSameInstanceAs(fake)
      }

      "must leave the value unchanged" in {
        provider(disabled).get().encrypt(PlainText("AA123456A")).value mustBe "AA123456A"
      }

      "must not require a valid key" in {
        val crypto = provider(
          Map(
            "mongodb.encryption.enabled" -> false,
            "mongodb.encryption.key" -> "not-a-key"
          )
        ).get()

        crypto.encrypt(PlainText("value")).value mustBe "value"
      }
    }

    "when the enabled flag is absent" - {

      "must default to encrypting" in {
        val crypto = provider(Map("mongodb.encryption.key" -> testKey)).get()

        crypto must not be theSameInstanceAs(fake)
        crypto.encrypt(PlainText("value")).value must not be "value"
      }

      "must fail rather than silently fall back when there is no config at all" in {
        assertThrows[Exception] {
          provider(Map.empty).get()
        }
      }
    }
  }
}