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

import models.requests.*
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ManageLeaseController @Inject()(
                                       cc: ControllerComponents,
                                       override val executionContext: ExecutionContext
                                     ) extends BackendController(cc) with StubResource  with Logging{


  def createLease(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CreateLeaseRequest].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val fullPath = s"/resources.data.lease/createLeaseReturn.json"
        findResource(fullPath) match {
          case Some(content) =>
            Future.successful(jsonResourceAsResponse(fullPath))
          case _ =>
            Future.successful(NotFound)
        }
      }
    )
  }

  def updateLease(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UpdateLeaseRequest].fold(
      invalid =>
        logger.error(s"[ManageLeaseController][updateLease]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        val successResponse = Ok(Json.obj("updated" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))
        response.returnResourceRef match {
          case "errorUpdatingLease" => Future.successful(failureResponse)
          case _                    => Future.successful(successResponse)
        }
      }
    )
  }

  def removeLease(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DeleteLeaseRequest].fold(
      invalid =>
        logger.error(s"[ManageLeaseController][removeLease]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response =>
        val successResponse = Ok(Json.obj("deleted" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorRemovingLease" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
    )
  }

}
