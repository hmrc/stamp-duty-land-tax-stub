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

package uk.gov.hmrc.stampdutylandtaxstub.controllers

import jakarta.inject.Singleton
import models.requests.{CreatePurchaserRequest, DeletePurchaserRequest, UpdatePurchaserRequest}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManagePurchasersController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource with Logging {

  def createPurchaser(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CreatePurchaserRequest].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val fullPath = s"/resources.data.purchaser/createPurchaserReturn.json"

        findResource(fullPath) match {
          case Some(content) => Future.successful(jsonResourceAsResponse(fullPath))
          case _ => Future.successful(NotFound)
        }
      }
    )
  }

  def updatePurchaser(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UpdatePurchaserRequest].fold(
      invalid =>
        logger.error(s"[ManagePurchasersController][updatePurchaser]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response =>
        val successResponse = Ok(Json.obj("updated" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorUpdatingPurchaser" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
    )
  }

  def removePurchaser(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DeletePurchaserRequest].fold(
      invalid =>
        logger.error(s"[ManagePurchasersController][removePurchaser]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response =>
        val successResponse = Ok(Json.obj("deleted" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorRemovingPurchaser" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
    )
  }
}




