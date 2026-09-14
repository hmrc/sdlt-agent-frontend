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
import uk.gov.hmrc.crypto.{Crypted, PlainBytes, PlainText}

class FakeEncrypterDecrypterSpec extends AnyFreeSpec with Matchers {

  private val fake = new FakeEncrypterDecrypter()

  "FakeEncrypterDecrypter" - {

    ".encrypt" - {

      "must pass PlainText through unchanged" in {
        fake.encrypt(PlainText("some text")).value mustBe "some text"
      }

      "must pass PlainBytes through as UTF-8 text" in {
        fake.encrypt(PlainBytes("some text".getBytes("UTF-8"))).value mustBe "some text"
      }
    }

    ".decrypt" - {

      "must pass the value through unchanged" in {
        fake.decrypt(Crypted("some text")).value mustBe "some text"
      }
    }

    ".decryptAsBytes" - {

      "must return the UTF-8 bytes of the value" in {
        fake.decryptAsBytes(Crypted("some text")).value mustBe "some text".getBytes("UTF-8")
      }
    }

    "round trip" - {

      "must preserve text through encrypt then decrypt" in {
        fake.decrypt(fake.encrypt(PlainText("some text"))).value mustBe "some text"
      }

      "must preserve non-ASCII characters" in {
        fake.decrypt(fake.encrypt(PlainText("café £100"))).value mustBe "café £100"
      }
    }
  }
}