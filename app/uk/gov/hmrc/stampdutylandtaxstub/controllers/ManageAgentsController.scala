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
import models.requests.{StornAndArnRequest, StornRequest}
import models.response.SubmitAgentDetailsResponse
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ManageAgentsController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource with Logging:

  def getAgentDetails: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornAndArnRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][getAgentDetails]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {

        val basePath = "/resources.manage.agentDetails"

        val fullPath = s"$basePath/${response.storn}/${response.agentReferenceNumber}/manageAgentDetails.json"

        findResource(fullPath) match {
          case Some(content) =>
            logger.info("[ManageAgentsController][getAgentDetails]: Json resource not found")
            Future.successful(jsonResourceAsResponse(fullPath))
          case _             =>
            logger.error("[ManageAgentsController][getAgentDetails]: Json resource not found")
            Future.successful(NotFound)
        }
      }
    )
  }

  def removeAgent: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornAndArnRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][removeAgent]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {

        val basePath = "/resources.manage.agentDetails"

        val fullPath = s"$basePath/${response.storn}/${response.agentReferenceNumber}/manageAgentDetails.json"

        findResource(fullPath) match {
          case Some(content) =>
            logger.info("[ManageAgentsController][removeAgent]: Successfully retrieved agent - sending dummy true delete")
            Future.successful(Ok(Json.toJson(true)))
          case _ =>
            logger.error("[ManageAgentsController][removeAgent]: Json resource not found")
            Future.successful(NotFound)
        }
      }
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
            UUID.randomUUID().toString
          )
        )))
      }
    )
  }

  def getAllAgentsLegacy: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][getAllAgents]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {

        val basePath = "/resources.manage.allAgentDetails"

        val fullPath = s"$basePath/${response.storn}/manageAllAgentDetails.json"

        findResource(fullPath) match {
          case Some(content) =>
            logger.info("[ManageAgentsController][getAllAgentsLegacy]: Successfully retrieved json resource")
            Future.successful(jsonResourceAsResponse(fullPath))
          case _ =>
            logger.error("[ManageAgentsController][getAllAgentsLegacy]: Json resource not found")
            Future.successful(NotFound)
        }
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
          case _ =>
            logger.error("[ManageAgentsController][getSdltOrganisation]: Json resource not found")
            Future.successful(NotFound)
        }
      }
    )
  }

  def getAllReturns: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornRequest].fold(
      invalid =>
        logger.error(s"[ManageAgentsController][getAllReturns]: Failed to validate payload, errors: $invalid")
        Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      response => {

        val basePath = "/resources.manage.allReturns"

        val fullPath = s"$basePath/${response.storn}/returnResponse.json"

        findResource(fullPath) match {
          case Some(content) =>
            logger.info("[ManageAgentsController][getAllReturns]: Successfully retrieved json resource")
            Future.successful(jsonResourceAsResponse(fullPath))
          case _ =>
            logger.error("[ManageAgentsController][getAllReturns]: Json resource not found")
            Future.successful(NotFound)
        }
      }
    )
  }
