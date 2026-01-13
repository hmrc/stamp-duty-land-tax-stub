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
import models.requests.{CreateCompanyDetailsRequest, DeleteCompanyDetailsRequest, UpdateCompanyDetailsRequest}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManageCompanyDetailsController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource with Logging {

  def createCompanyDetails(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CreateCompanyDetailsRequest].fold(
      invalid =>
        logger.error(s"[ManageCompanyDetailsController][createCompanyDetails]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {
        Future.successful(Ok(Json.obj("companyDetailsId" -> "221110170")))
        }
    )
  }

  def updateCompanyDetails(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UpdateCompanyDetailsRequest].fold(
      invalid =>
        logger.error(s"[ManageCompanyDetailsController][updateCompanyDetails]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response =>
        val successResponse = Ok(Json.obj("updated" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorUpdatingCompanyDetails" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
    )
  }

  def removeCompanyDetails(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DeleteCompanyDetailsRequest].fold(
      invalid =>
        logger.error(s"[ManageCompanyDetailsController][removeCompanyDetails]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response =>
        val successResponse = Ok(Json.obj("deleted" -> true))
        val failureResponse = BadRequest(Json.obj("message" -> "Something went wrong"))

        response.returnResourceRef match {
          case "errorRemovingCompanyDetails" =>
            Future.successful(failureResponse)
          case _ =>
            Future.successful(successResponse)
        }
    )
  }

}
