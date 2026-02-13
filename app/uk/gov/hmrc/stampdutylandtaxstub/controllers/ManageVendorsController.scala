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

import models.requests.{DeleteVendorRequest, CreateVendorRequest, UpdateVendorRequest}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ManageVendorsController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource with Logging:

  def createVendor: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CreateVendorRequest].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val fullPath = s"/resources.data.vendor/createVendorReturn.json"

        findResource(fullPath) match {
          case Some(content) => Future.successful(jsonResourceAsResponse(fullPath)) 
          case _ => Future.successful(NotFound)
        }
      }
    )
  }

  def updateVendor(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UpdateVendorRequest].fold(
      invalid =>
        logger.error(s"[ManageVendorsController][updateVendor]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val successResponse = Ok(Json.obj("updated" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))
        response.returnResourceRef match {
          case "errorUpdatingVendor" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
      }
    )
  }

  def removeVendor(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DeleteVendorRequest].fold(
      invalid =>
        logger.error(s"[ManageVendorsController][removeVendor]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val successResponse = Ok(Json.obj("deleted" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorRemovingVendor" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
      }
    )
  }
