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

import models.AgentDetails
import models.requests.StornRequest
import models.response.SubmitAgentDetailsResponse
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.StubResource

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ManageAgentsController @Inject()(cc: ControllerComponents, override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource:

  def getAgentDetails: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornRequest].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      storn => {

        val basePath = "/resources.manage.agentDetails"

        val fullPath = s"$basePath/${storn.storn}/manageAgentDetails.json"

        findResource(fullPath) match {
          case Some(content) => Future.successful(jsonResourceAsResponse(fullPath))
          case _             => Future.successful(NotFound)
        }
      }
    )
  }

  def submitAgentDetails: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[AgentDetails].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      _ => {
        Future.successful(Ok(Json.toJson(
          SubmitAgentDetailsResponse(
            UUID.randomUUID().toString
          )
        )))
      }
    )
  }

  def getAllAgents: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[StornRequest].fold(
      invalid => Future.successful(BadRequest(Json.obj("message" -> s"Invalid payload: $invalid"))),
      storn => {

        val basePath = "/resources.manage.allAgentDetails"

        val fullPath = s"$basePath/${storn.storn}/manageAllAgentDetails.json"

        findResource(fullPath) match {
          case Some(content) => Future.successful(jsonResourceAsResponse(fullPath))
          case _ => Future.successful(NotFound)
        }
      }
    )
  }
