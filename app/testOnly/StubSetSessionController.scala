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

package testOnly

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import pages.manageAgents.{AddressLookupPage, AgentContactDetailsPage, AgentNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class StubSetSessionController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 sessionRepository: SessionRepository,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 val controllerComponents: MessagesControllerComponents,
                                               )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def set(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val fields: Seq[(String, JsValue)] =
        request.queryString.collect {
          case (k, vs) if vs.nonEmpty =>
            val raw = vs.head
            val js: JsValue = Try(Json.parse(raw)).getOrElse(JsString(raw))
            k -> js
        }.toSeq

      val patch: JsObject = JsObject(fields)

      val updatedAnswers: UserAnswers =
        request.userAnswers.copy(
          data = request.userAnswers.data.deepMerge(patch)
        )

      sessionRepository.set(updatedAnswers).map { _ =>
        Ok(Json.obj(
          "status" -> "ok",
          "merged" -> patch,
          "resultingData" -> updatedAnswers.data
        ))
      }
    }

  def clear(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val updatedAnswers: UserAnswers =
        request.userAnswers.copy(
          data = Json.obj()
        )

      sessionRepository.set(updatedAnswers).map { _ =>
        Ok(Json.obj(
          "status" -> "ok",
          "resultingData" -> updatedAnswers.data
        ))
      }
    }
}
