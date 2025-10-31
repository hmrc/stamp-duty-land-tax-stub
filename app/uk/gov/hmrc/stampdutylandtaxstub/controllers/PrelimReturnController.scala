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

package uk.gov.hmrc.stampdutylandtaxstub.controllers

import models.PrelimReturn
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PrelimReturnController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource:

  val basePath = "/resources.data.filing.prelim"

  def prelimReturnDetails(returnId: Option[String]): Action[AnyContent] = Action {
    returnId match {
      case Some(id) => {
        findResource(s"$basePath/$id/prelimReturnDetails.json") match {
          case Some(content) => jsonResourceAsResponse(s"$basePath/$id/prelimReturnDetails.json")
          case _ => NotFound
        }
      }
      case _ => NotFound
    }
  }

  def submitPrelimReturns: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[PrelimReturn].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      _ => {
        Future.successful(
          findResource(s"$basePath/returnId.json") match {
            case Some(content) => jsonResourceAsResponse(s"$basePath/returnId.json")
            case _ => NotFound
          }
        )
      }
    )
  }
