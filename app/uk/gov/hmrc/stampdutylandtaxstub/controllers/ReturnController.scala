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

import models.requests.{GetReturnByRefRequest, PrelimReturn, ReturnVersionUpdateRequest}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnController @Inject()(
                                        cc: ControllerComponents,
                                        override val executionContext: ExecutionContext
                                      ) extends BackendController(cc) with StubResource {

  val basePath = "/resources/data/filing.prelim"
  val basePathFull = "/resources/data/filing.full"

  def prelimReturnDetails: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[PrelimReturn].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val fullPath = s"/resources.data.filing.prelim/123456/prelimReturnDetails.json"

        findResource(fullPath) match {
          case Some(content) => Future.successful(jsonResourceAsResponse(fullPath))
          case _ => Future.successful(NotFound)
        }
      }
    )
  }

  def getFullReturn: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[GetReturnByRefRequest].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val fullPath = s"$basePathFull/${response.returnResourceRef}/fullReturnDetails.json"

        findResource(fullPath) match {
          case Some(content) => Future.successful(jsonResourceAsResponse(fullPath))
          case _ => Future.successful(NotFound)
        }
      }
    )
  }

  def updateReturnVersion(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[ReturnVersionUpdateRequest].fold(
      invalid =>
        logger.error(s"[ReturnVersionController][updateReturnVersion]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val successResponse = Ok(Json.obj("updated" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorUpdatingReturnVersion" => Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
      }
    )
  }
}
