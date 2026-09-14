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

import play.api.libs.json._
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

/*
 * UserAnswersEncrypted is a wrapper for UserAnswers that encrypts the data field for secure
 * storage in MongoDB. It is only used in the repository layer, and converted to/from UserAnswers
 * when reading/writing to the database. The encryption/decryption is handled automatically by
 * the implicit Reads and Writes using the provided Encrypter/Decrypter.
 */

final case class UserAnswersEncrypted(
                                       id: String,
                                       data: SensitiveWrapper[JsObject] = SensitiveWrapper(Json.obj()),
                                       lastUpdated: Instant = Instant.now
                                     ) {

  def toUserAnswers: UserAnswers =
    UserAnswers(
      id = id,
      data = data.decryptedValue,
      lastUpdated = lastUpdated
    )
}

object UserAnswersEncrypted {

  def fromUserAnswers(userAnswers: UserAnswers): UserAnswersEncrypted =
    UserAnswersEncrypted(
      id = userAnswers.id,
      data = SensitiveWrapper(userAnswers.data),
      lastUpdated = userAnswers.lastUpdated
    )

  implicit def reads(implicit crypto: Encrypter & Decrypter): Reads[UserAnswersEncrypted] = {
    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[SensitiveWrapper[JsObject]] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(UserAnswersEncrypted.apply _)
  }

  implicit def writes(implicit crypto: Encrypter & Decrypter): Writes[UserAnswersEncrypted] = {
    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[SensitiveWrapper[JsObject]] and
        (__ \ "lastUpdated").write[Instant](MongoJavatimeFormats.instantFormat)
      )(ua => (ua.id, ua.data, ua.lastUpdated))
  }

  implicit def format(implicit crypto: Encrypter & Decrypter): Format[UserAnswersEncrypted] =
    Format(reads, writes)
}