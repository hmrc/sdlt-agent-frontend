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
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.time.Instant
import java.time.temporal.ChronoUnit

class UserAnswersEncryptedSpec extends AnyFreeSpec with Matchers with EitherValues with OptionValues {

  private implicit val crypto: Encrypter with Decrypter =
    SymmetricCryptoFactory.aesGcmCrypto("z9WMSuFsHqfY5F2wgIcEvcnwyRTRB4dyPWfMbCbCXfM=")

  // MongoJavatimeFormats stores millisecond precision, so anything finer is lost on round-trip
  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)

  private val data = Json.obj(
    "purchaserCurrent" -> Json.obj(
      "whoIsMakingThePurchase" -> "Individual",
      "nationalInsuranceNumber" -> "AA123456A"
    )
  )

  private val userAnswers = UserAnswers(
    id = "test-session-id",
    data = data,
    lastUpdated = instant
  )

  private val userAnswersEncrypted = UserAnswersEncrypted.fromUserAnswers(userAnswers)

  ".fromUserAnswers" - {

    "must wrap the data field and copy the rest across" in {
      userAnswersEncrypted.id mustBe "test-session-id"
      userAnswersEncrypted.data.decryptedValue mustBe data
      userAnswersEncrypted.lastUpdated mustBe instant
    }

    "must handle an empty data object" in {
      val result = UserAnswersEncrypted.fromUserAnswers(userAnswers.copy(data = Json.obj()))

      result.data.decryptedValue mustBe Json.obj()
    }
  }

  ".toUserAnswers" - {

    "must unwrap the data field and copy the rest across" in {
      userAnswersEncrypted.toUserAnswers mustEqual userAnswers
    }

    "must handle an empty data object" in {
      val empty = userAnswers.copy(data = Json.obj())

      UserAnswersEncrypted.fromUserAnswers(empty).toUserAnswers mustEqual empty
    }
  }

  "UserAnswersEncrypted" - {

    ".reads" - {

      "must be found implicitly" in {
        implicitly[Reads[UserAnswersEncrypted]]
      }

      "must deserialize and decrypt a stored document" in {
        val json = Json.toJson(userAnswersEncrypted)
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.data.decryptedValue mustBe data
      }

      "must fail to deserialize when _id is missing" in {
        val json = Json.toJson(userAnswersEncrypted).as[JsObject] - "_id"
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when data is missing" in {
        val json = Json.toJson(userAnswersEncrypted).as[JsObject] - "data"
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither

        result.isLeft mustBe true
      }

      "must fail to deserialize when the ciphertext is not valid" in {
        val json = Json.toJson(userAnswersEncrypted).as[JsObject] ++ Json.obj("data" -> "not-encrypted")

        assertThrows[Exception] {
          Json.fromJson[UserAnswersEncrypted](json)
        }
      }
    }

    ".writes" - {

      "must be found implicitly" in {
        implicitly[Writes[UserAnswersEncrypted]]
      }

      "must write the data field as an opaque string" in {
        (Json.toJson(userAnswersEncrypted) \ "data").get mustBe a[JsString]
      }

      "must leave the id in plaintext" in {
        (Json.toJson(userAnswersEncrypted) \ "_id").as[String] mustBe "test-session-id"
      }

      "must not leak sensitive values into the serialized document" in {
        val raw = Json.toJson(userAnswersEncrypted).toString

        raw must not include "AA123456A"
        raw must not include "purchaserCurrent"
        raw must not include "Individual"
      }
    }

    ".formats" - {

      "must be found implicitly" in {
        implicitly[Format[UserAnswersEncrypted]]
      }

      "must round-trip a UserAnswers without losing anything" in {
        val json = Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers))
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.toUserAnswers mustEqual userAnswers
      }

      "must round-trip a minimal UserAnswers" in {
        val minimal = UserAnswers(id = "id", lastUpdated = instant)
        val json = Json.toJson(UserAnswersEncrypted.fromUserAnswers(minimal))
        val result = Json.fromJson[UserAnswersEncrypted](json).asEither.value

        result.toUserAnswers mustEqual minimal
      }

      "must produce different ciphertext for the same value on each write" in {
        val first = (Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers)) \ "data").as[String]
        val second = (Json.toJson(UserAnswersEncrypted.fromUserAnswers(userAnswers)) \ "data").as[String]

        first must not equal second
      }
    }
  }
}