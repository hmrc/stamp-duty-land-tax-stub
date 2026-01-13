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

import models.requests.{CreateCompanyDetailsRequest, DeleteCompanyDetailsRequest, UpdateCompanyDetailsRequest}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ManageCompanyDetailsControllerSpec extends AnyFreeSpec
  with Matchers
  with GuiceOneServerPerSuite
  with MockitoSugar {

  implicit val system: ActorSystem = app.actorSystem

  lazy val testController: ManageCompanyDetailsController = app.injector.instanceOf[ManageCompanyDetailsController]
  
  "ManageCompanyDetailsController" - {
    ".createCompanyDetails" - {
      val validCreateCompanyDetailsRequest = CreateCompanyDetailsRequest(
        stornId = "STORN12345",
        returnResourceRef = "123456",
        purchaserResourceRef = "123456"
      )

      val fakeCreateCompanyDetailsPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validCreateCompanyDetailsRequest))

      val invalidCreateCompanyDetailsPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid and resource exists" in {

        val result = testController.createCompanyDetails()(fakeCreateCompanyDetailsPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is invalid" in {
        val result = testController.createCompanyDetails()(invalidCreateCompanyDetailsPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.createCompanyDetails()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

    }

    ".updateCompanyDetails" - {
      val validUpdateCompanyDetailsRequest = UpdateCompanyDetailsRequest(
        stornId = "STORN12345",
        purchaserResourceRef = "12345",
        returnResourceRef = "123456"
      )

      val fakeUpdateCompanyDetailsPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdateCompanyDetailsRequest))

      val fakeUpdateCompanyDetailsErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdateCompanyDetailsRequest.copy(returnResourceRef = "errorUpdatingCompanyDetails")))

      val invalidUpdateCompanyDetailsPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid and resource exists" in {
        val result = testController.updateCompanyDetails()(fakeUpdateCompanyDetailsPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorUpdatingCompanyDetails" in {
        val result = testController.updateCompanyDetails()(fakeUpdateCompanyDetailsErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.updateCompanyDetails()(invalidUpdateCompanyDetailsPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.updateCompanyDetails()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }

    ".removeCompanyDetails" - {
      val validRemoveCompanyDetailsRequest = DeleteCompanyDetailsRequest(
        storn = "STORN12345",
        returnResourceRef = "123456"
      )

      val fakeRemoveCompanyDetailsPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validRemoveCompanyDetailsRequest))

      val fakeRemoveCompanyDetailsErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validRemoveCompanyDetailsRequest.copy(returnResourceRef = "errorRemovingCompanyDetails")))

      val invalidRemoveCompanyDetailsPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid and resource exists" in {
        val result = testController.removeCompanyDetails()(fakeRemoveCompanyDetailsPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorRemovingCompanyDetails" in {
        val result = testController.removeCompanyDetails()(fakeRemoveCompanyDetailsErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.removeCompanyDetails()(invalidRemoveCompanyDetailsPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.removeCompanyDetails()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }
  }
}
