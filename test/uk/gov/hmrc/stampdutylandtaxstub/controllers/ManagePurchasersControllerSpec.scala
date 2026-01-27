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

import models.requests.{CreatePurchaserRequest, DeletePurchaserRequest, UpdatePurchaserRequest}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.http.Status

class ManagePurchasersControllerSpec extends AnyFreeSpec
  with Matchers
  with GuiceOneServerPerSuite
  with MockitoSugar {

  implicit val system: ActorSystem = app.actorSystem

  lazy val testController: ManagePurchasersController = app.injector.instanceOf[ManagePurchasersController]

  "ManagePurchaserController" - {
    ".createPurchaser" - {
      val validCreatePurchaserRequest = CreatePurchaserRequest(
        stornId = "STORN12345",
        returnResourceRef = "123456",
        isCompany = "Yes",
        isTrustee = "No",
        isConnectedToVendor = "No",
        isRepresentedByAgent = "Yes",
        address1 = "Address"
      )

      val fakeCreatePurchaserPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validCreatePurchaserRequest))

      val invalidCreatePurchaserPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))
      
      "return 200 when payload is valid and resource exists" in {
        
        val result = testController.createPurchaser()(fakeCreatePurchaserPOSTRequest)

        status(result) shouldBe Status.OK
      }
      
      "return 400 when payload is invalid" in {
        val result = testController.createPurchaser()(invalidCreatePurchaserPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.createPurchaser()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

    }
    
    ".updatePurchaser" - {
      val validUpdatePurchaserRequest = UpdatePurchaserRequest(
        stornId = "STORN12345",
        purchaserResourceRef = "12345",
        returnResourceRef = "123456",
        isCompany = Some("Yes"),
        isTrustee = Some("No"),
        isConnectedToVendor = Some("No"),
        isRepresentedByAgent = Some("Yes"),
        address1 = Some("Address")
      )

      val fakeUpdatePurchaserPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdatePurchaserRequest))
      
      val fakeUpdatePurchaserErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdatePurchaserRequest.copy(returnResourceRef = "errorUpdatingPurchaser")))
          
      val invalidUpdatePurchaserPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))
          
      "return 200 when payload is valid and resource exists" in {
        val result = testController.updatePurchaser()(fakeUpdatePurchaserPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorUpdatingPurchaser" in {
        val result = testController.updatePurchaser()(fakeUpdatePurchaserErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.updatePurchaser()(invalidUpdatePurchaserPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.updatePurchaser()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }
    
    ".removePurchaser" - {
      val validRemovePurchaserRequest = DeletePurchaserRequest(
        storn = "STORN12345",
        purchaserResourceRef = "12345",
        returnResourceRef = "123456"
)

      val fakeRemovePurchaserPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validRemovePurchaserRequest))

      val fakeRemovePurchaserErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validRemovePurchaserRequest.copy(returnResourceRef = "errorRemovingPurchaser")))

      val invalidRemovePurchaserPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid and resource exists" in {
        val result = testController.removePurchaser()(fakeRemovePurchaserPOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorRemovingPurchaser" in {
        val result = testController.removePurchaser()(fakeRemovePurchaserErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.removePurchaser()(invalidRemovePurchaserPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.removePurchaser()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }
  }
}
