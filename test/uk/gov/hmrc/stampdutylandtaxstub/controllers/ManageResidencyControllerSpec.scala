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

import models.requests.{CreateResidencyRequest, ResidencyPayload, UpdateResidencyRequest}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ManageResidencyControllerSpec extends AnyFreeSpec
  with Matchers
  with GuiceOneServerPerSuite
  with MockitoSugar {

  implicit val system: ActorSystem = app.actorSystem

  lazy val testController: ManageResidencyController = app.injector.instanceOf[ManageResidencyController]

  private val validResidencyPayload = ResidencyPayload(
    isNonUkResidents = "true",
    isCompany = "false",
    isCrownRelief = "false"
  )

  "ManageResidencyController" - {
    ".createResidency" - {
      val validCreateResidencyRequest = CreateResidencyRequest(
        stornId = "STORN12345",
        returnResourceRef = "123456",
        residency = validResidencyPayload
      )

      val fakeCreateResidencyPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validCreateResidencyRequest))

      val invalidCreateResidencyPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid and resource exists" in {
        val result = testController.createResidency()(fakeCreateResidencyPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is invalid" in {
        val result = testController.createResidency()(invalidCreateResidencyPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.createResidency()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }

    ".updateResidency" - {
      val validUpdateResidencyRequest = UpdateResidencyRequest(
        stornId = "STORN12345",
        returnResourceRef = "123456",
        residency = validResidencyPayload
      )

      val fakeUpdateResidencyPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdateResidencyRequest))

      val fakeUpdateResidencyErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdateResidencyRequest.copy(returnResourceRef = "errorUpdatingResidency")))

      val invalidUpdateResidencyPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid" in {
        val result = testController.updateResidency()(fakeUpdateResidencyPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorUpdatingResidency" in {
        val result = testController.updateResidency()(fakeUpdateResidencyErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.updateResidency()(invalidUpdateResidencyPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.updateResidency()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }
  }
}
