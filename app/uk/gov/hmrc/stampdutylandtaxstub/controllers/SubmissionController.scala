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

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.util.{PollCounter, StubResource}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SubmissionController @Inject()(cc: ControllerComponents,
                                     pollCounter: PollCounter,
                                     override val executionContext: ExecutionContext)
  extends BackendController(cc) with StubResource with Logging:

  private implicit val ec: ExecutionContext = executionContext

  def submit(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val ref = (request.body \ "fullReturn" \ "returnResourceRef").asOpt[String].getOrElse("")
    logger.info(s"[Stub][submit] returnResourceRef=$ref")

    if (pollCounter.isTimedOut(ref)) {
      logger.info(s"[Stub][submit] poll timeout for returnResourceRef=$ref")
      Future.successful(RequestTimeout(Json.obj("returnId" -> ref, "_type" -> "failed")))
    } else {

      val result = ref match {
        case "acknowledged" =>
          Accepted(Json.obj("returnId" -> ref, "_type" -> "acknowledged"))

        case "retryable" =>
          ServiceUnavailable(Json.obj("returnId" -> ref, "_type" -> "retryable"))

        case "rejected" =>
          BadRequest(Json.obj(
            "returnId" -> ref,
            "_type"    -> "rejected",
            "errors"   -> Json.arr(
              Json.obj("code" -> "3001", "message" -> "Business validation failed", "location" -> "/purchaser[1]"),
              Json.obj("code" -> "3002", "message" -> "Consideration mismatch")
            )
          ))

        case "failed" | "error" =>
          BadGateway(Json.obj(
            "returnId" -> ref,
            "_type"    -> "failed",
            "errors"   -> Json.arr(
              Json.obj("code" -> "5001", "message" -> "Simulated transport error")
            )
          ))

        case "no-receipt" =>
          Ok(Json.obj("returnId" -> ref, "utrn" -> "123456789MA", "receipt" -> false, "_type" -> "submitted"))

        case _ =>
          Ok(Json.obj("returnId" -> ref, "utrn" -> "123456789MA", "receipt" -> true, "_type" -> "submitted"))
      }

      Future.successful(result)
    }
  }