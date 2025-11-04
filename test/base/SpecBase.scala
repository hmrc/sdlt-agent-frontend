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

package base

import controllers.actions.*
import models.UserAnswers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.manageAgents.StornPage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val testArn = "ARN001"

  val testStorn = "STN001"
  
  val userAnswersId: String = "id"

  val stornData = Json.obj(
    "storn" -> "STN001"
  )
  
  def emptyUserAnswers : UserAnswers = UserAnswers(userAnswersId, stornData)

  val emptyUserAnswersWithStorn: UserAnswers = emptyUserAnswers.set(StornPage, testStorn).success.value

  val testUserAnswers: JsObject = Json.obj(
    "agentName" -> "John",
    "agentAddress" -> "123 Road",
    "agentContactDetails" -> Json.obj(
      "contactTelephoneNumber" -> "07123456789",
      "contactEmail" -> "john@example.com"
    ),
    "agentAddress" -> Json.obj(
      "auditRef" -> "d8819c6a-8d78-4219-8f9d-40b119edcb3d",
      "address" -> Json.obj(
        "lines" -> Json.arr(
          "10 Downing Street",
          "South Kensington",
          "London",
          "SW7 5JT"
        )
      )
    )
  )


  val populatedUserAnswers: UserAnswers =
    emptyUserAnswersWithStorn.copy(data = emptyUserAnswersWithStorn.data ++ testUserAnswers)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
}
