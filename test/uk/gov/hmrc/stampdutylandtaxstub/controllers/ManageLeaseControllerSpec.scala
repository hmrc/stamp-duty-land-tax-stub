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

import models.requests.{CreateLeaseRequest, DeleteLeaseRequest, UpdateLeaseRequest}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ManageLeaseControllerSpec extends AnyFreeSpec
  with Matchers
  with GuiceOneServerPerSuite
  with MockitoSugar {

  implicit val system: ActorSystem = app.actorSystem

  lazy val testController: ManageLeaseController = app.injector.instanceOf[ManageLeaseController]

  "ManageLeaseController" - {
    ".createLease" - {
      val validCreateLeaseRequest = CreateLeaseRequest(
        stornId = "STORN12345",
        returnResourceRef = "123456",
        isAnnualRentOver1000 = Some("yes"), 
        contractEndDate = Some("2025 01 01"), 
        contractStartDate = Some("2000 01 01"), 
        leaseType = Some("M"), 
        netPresentValue = Some("50"), 
        totalPremiumPayable = Some("50"), 
        rentFreePeriod = Some("50"), 
        startingRent = Some("50"), 
        startingRentEndDate = Some("2024 01 01"), 
        laterRentKnown = Some("yes"), 
        vatAmount = Some("50")
      )

      val fakeCreateLeasePOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validCreateLeaseRequest))

      val invalidCreateLeasePOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))
      
      "return 200 when payload is valid and resource exists" in {
        
        val result = testController.createLease()(fakeCreateLeasePOSTRequest)

        status(result) shouldBe Status.OK
      }
      
      "return 400 when payload is invalid" in {
        val result = testController.createLease()(invalidCreateLeasePOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.createLease()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

    }
    
    ".updateLease" - {
      val validUpdateLeaseRequest = UpdateLeaseRequest(
        stornId = "STORN12345",
        returnResourceRef = "123456",
        isAnnualRentOver1000 = Some("no"),
        contractEndDate = Some("2025 01 01"),
        contractStartDate = Some("2000 01 01"),
        leaseType = Some("M"),
        netPresentValue = Some("50"),
        totalPremiumPayable = Some("50"),
        rentFreePeriod = Some("50"),
        startingRent = Some("50"),
        startingRentEndDate = Some("2024 01 01"),
        laterRentKnown = Some("yes"),
        vatAmount = Some("50")
      )

      val fakeUpdateLeasePOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdateLeaseRequest))
      
      val fakeUpdateLeaseErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validUpdateLeaseRequest.copy(returnResourceRef = "errorUpdatingLease")))
          
      val invalidUpdateLeasePOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))
          
      "return 200 when payload is valid and resource exists" in {
        val result = testController.updateLease()(fakeUpdateLeasePOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorUpdatingLease" in {
        val result = testController.updateLease()(fakeUpdateLeaseErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.updateLease()(invalidUpdateLeasePOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.updateLease()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }
    
    ".removeLease" - {
      val validRemoveLeaseRequest = DeleteLeaseRequest(
        storn = "STORN12345",
        returnResourceRef = "123456",
        leaseResourceRef = "123456"
)

      val fakeRemoveLeasePOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validRemoveLeaseRequest))

      val fakeRemoveLeaseErrorPOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(validRemoveLeaseRequest.copy(returnResourceRef = "errorRemovingLease")))

      val invalidRemoveLeasePOSTRequest =
        FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(""))

      "return 200 when payload is valid and resource exists" in {
        val result = testController.removeLease()(fakeRemoveLeasePOSTRequest)

        status(result) shouldBe Status.OK
      }

      "return 400 when payload is valid and returnResourceRef is errorRemovingLease" in {
        val result = testController.removeLease()(fakeRemoveLeaseErrorPOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return 400 when payload is invalid" in {
        val result = testController.removeLease()(invalidRemoveLeasePOSTRequest)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }

      "return 400 when required fields are missing" in {
        val invalidRequest = Json.obj("returnResourceRef" -> "123456")
        val request = FakeRequest("POST", "/")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(invalidRequest)

        val result = testController.removeLease()(request)

        status(result) shouldBe Status.BAD_REQUEST
        (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
      }
    }
  }
}
