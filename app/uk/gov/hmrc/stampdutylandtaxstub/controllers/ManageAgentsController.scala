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

import models.AgentDetailsBeforeCreation
import models.requests.{SdltReturnRecordRequest, StornAndArnRequest, StornRequest}
import models.response.SubmitAgentDetailsResponse
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ManageAgentsController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource with Logging:

  def deletePredefinedAgent: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornAndArnRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][deletePredefinedAgent]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response =>
        logger.info("[ManageAgentsController][deletePredefinedAgent]: Successfully validated payload - sending dummy deleted JSON Boolean")
        Future.successful(Ok(Json.obj("deleted" -> true)))
    )
  }

  def submitAgentDetails: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[AgentDetailsBeforeCreation].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][submitAgentDetails]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      _ => {
        logger.info("[ManageAgentsController][submitAgentDetails]: Json validation successful for AgentDetailsBeforeCreation")
        Future.successful(Ok(Json.toJson(
          SubmitAgentDetailsResponse(
            UUID.randomUUID().toString,
            UUID.randomUUID().toString,
          )
        )))
      }
    )
  }

  def getSdltOrganisation: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][getSdltOrganisation]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {

        val basePath = "/resources.manage.getSdltOrganisation"

        val fullPath = s"$basePath/${response.storn}/returnResponse.json"

        findResource(fullPath) match {
          case Some(content) =>
            logger.info("[ManageAgentsController][getSdltOrganisation]: Successfully retrieved json resource")
            Future.successful(jsonResourceAsResponse(fullPath))
          case err =>
            logger.error(s"[ManageAgentsController][getSdltOrganisation]: Json resource not found: $err")
            Future.successful(NotFound)
        }
      }
    )
  }

  def getReturns: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[SdltReturnRecordRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][getReturns]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {

        val fullPath = response match {
          case SdltReturnRecordRequest(_,     _,                            true,  Some("SUBMITTED"),   _) => s"/resources.manage.getReturns/${response.storn}/deleted/deletedSubmittedReturns.json"
          case SdltReturnRecordRequest(_,     _,                            true,  Some("IN-PROGRESS"), _) => s"/resources.manage.getReturns/${response.storn}/deleted/deletedInProgressReturns.json"
          case SdltReturnRecordRequest(_,     Some("ACCEPTED"),             false, Some("IN-PROGRESS"), _) => s"/resources.manage.getReturns/${response.storn}/inProgress/acceptedReturns.json"
          case SdltReturnRecordRequest(_,     Some("STARTED"),              false, Some("IN-PROGRESS"), _) => s"/resources.manage.getReturns/${response.storn}/inProgress/startedReturns.json"
          case SdltReturnRecordRequest(_,     Some("PENDING"),              false, Some("IN-PROGRESS"), _) => s"/resources.manage.getReturns/${response.storn}/inProgress/pendingReturns.json"
          case SdltReturnRecordRequest(_,     Some("SUBMITTED"),            false, Some("SUBMITTED"),   _) => s"/resources.manage.getReturns/${response.storn}/submitted/submittedReturns.json"
          case SdltReturnRecordRequest(_,     Some("SUBMITTED_NO_RECEIPT"), false, Some("SUBMITTED"),   _) => s"/resources.manage.getReturns/${response.storn}/submitted/submittedNoReceiptReturns.json"
        }

        findResource(fullPath) match {
          case Some(content) =>
            logger.info(s"[ManageAgentsController][getReturns]: Successfully retrieved json resource: $content")
            Future.successful(jsonResourceAsResponse(fullPath))
          case err =>
            logger.error(s"[ManageAgentsController][getReturns]: Json resource not found: $err")
            Future.successful(NotFound)
        }
      }
    )
  }
