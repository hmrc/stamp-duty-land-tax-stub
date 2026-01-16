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

import models.requests.{GetReturnByRefRequest, PrelimReturn, ReturnInfoRequest, ReturnVersionUpdateRequest}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import play.api.test.FakeRequest

class ReturnControllerSpec
  extends AnyWordSpec
    with Matchers
    with GuiceOneServerPerSuite
    with MockitoSugar {

  implicit val system: ActorSystem = app.actorSystem

  private val validPrelimReturn = PrelimReturn(
    stornId = "12345",
    purchaserIsCompany = "YES",
    surNameOrCompanyName = "Test Company",
    houseNumber = Some(23),
    addressLine1 = "Test Street",
    addressLine2 = Some("Apartment 5"),
    addressLine3 = Some("Building A"),
    addressLine4 = Some("District B"),
    postcode = Some("TE23 5TT"),
    transactionType = "O"
  )

  private val validGetReturnByRefRequest = GetReturnByRefRequest(
    returnResourceRef = "123456",
    storn = "STORN12345"
  )

  private val validUpdateReturnVersionRequest = ReturnVersionUpdateRequest(
    storn = "STORN12345",
    returnResourceRef = "123456",
    currentVersion = "1.0"
  )

  private val validUpdateReturnInfoRequest = ReturnInfoRequest(
    storn = "STORN12345",
    returnResourceRef = "123456",
    mainPurchaserID = Some("PURCH123")
  )

  private val fakePrelimReturnPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validPrelimReturn))

  private val invalidPrelimReturnPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.obj("invalid" -> "data"))

  private val fakeGetFullReturnPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validGetReturnByRefRequest))

  private val invalidGetFullReturnPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(""))

  private val fakeUpdateReturnVersionPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validUpdateReturnVersionRequest))

  private val fakeUpdateReturnVersionErrorPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validUpdateReturnVersionRequest.copy(returnResourceRef = "errorUpdatingReturnVersion")))

  private val invalidUpdateReturnVersionPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(""))

  private val fakeUpdateReturnInfoPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validUpdateReturnInfoRequest))

  private val fakeUpdateReturnInfoErrorPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validUpdateReturnInfoRequest.copy(returnResourceRef = "errorUpdatingReturnInfo")))

  private val invalidUpdateReturnInfoPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(""))

  private val validGetSdltOrganisationPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.obj("storn" -> "STN005"))

  private val invalidGetSdltOrganisationPOSTRequest = {
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson("invalid" -> "payload"))
  }

  private val nonExistentGetSdltOrganisationPOSTRequest = {
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.obj("storn" -> "NON_EXISTENT_STORN"))
  }

  lazy val testController: ReturnController = app.injector.instanceOf[ReturnController]

  val returnIdJson: JsValue = Json.parse(
    """{
      "returnResourceRef":"123456"
    }"""
  )

  ".prelimReturnDetails" should {

    "return 200 when payload is valid and resource exists" in {
      val result = testController.prelimReturnDetails(fakePrelimReturnPOSTRequest)

      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe returnIdJson
    }

    "return 400 when payload is invalid" in {
      val result = testController.prelimReturnDetails(invalidPrelimReturnPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }

    "return 200 when resource file exists (controller always returns 200 for valid payload)" in {
      val nonExistentPrelimReturn = validPrelimReturn.copy(stornId = "99999")
      val request = FakeRequest("POST", "/")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.toJson(nonExistentPrelimReturn))

      val result = testController.prelimReturnDetails(request)

      status(result) shouldBe Status.OK
    }
    
  }

  ".getFullReturn" should {

    "return 200 when payload is valid and resource exists" in {
      val result = testController.getFullReturn(fakeGetFullReturnPOSTRequest)

      status(result) shouldBe Status.OK
    }

    "return 400 when payload is invalid" in {
      val result = testController.getFullReturn(invalidGetFullReturnPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }

    "return 404 when resource file is not found" in {
      val nonExistentRequest = GetReturnByRefRequest(
        returnResourceRef = "NON-EXISTENT-999",
        storn = "STORNXXX"
      )
      val request = FakeRequest("POST", "/")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.toJson(nonExistentRequest))

      val result = testController.getFullReturn(request)

      status(result) shouldBe Status.NOT_FOUND
    }

    "return 400 when required fields are missing" in {
      val invalidRequest = Json.obj("returnResourceRef" -> "123456")
      val request = FakeRequest("POST", "/")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(invalidRequest)

      val result = testController.getFullReturn(request)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }
  }

  ".updateReturnVersion" should {

    "return 200 when payload is valid and resource exists" in {
      val result = testController.updateReturnVersion()(fakeUpdateReturnVersionPOSTRequest)

      status(result) shouldBe Status.OK
    }

    "return 400 when payload is valid and returnResourceRef is errorUpdatingReturnVersion" in {
      val result = testController.updateReturnVersion()(fakeUpdateReturnVersionErrorPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 400 when payload is invalid" in {
      val result = testController.updateReturnVersion()(invalidUpdateReturnVersionPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }

    "return 400 when required fields are missing" in {
      val invalidRequest = Json.obj("returnResourceRef" -> "123456")
      val request = FakeRequest("POST", "/")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(invalidRequest)

      val result = testController.updateReturnVersion()(request)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }
  }

  ".updateReturnInfo" should {
    "return 200 when payload is valid and resource exists" in {
      val result = testController.updateReturnInfo()(fakeUpdateReturnInfoPOSTRequest)

      status(result) shouldBe Status.OK
    }

    "return 400 when payload is valid and returnResourceRef is errorUpdatingReturnInfo" in {
      val result = testController.updateReturnInfo()(fakeUpdateReturnInfoErrorPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 400 when payload is invalid" in {
      val result = testController.updateReturnInfo()(invalidUpdateReturnInfoPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }
  }

  ".getSdltOrganisation" should {

    "return 200 when payload is valid and resource exists" in {
      val result = testController.getSdltOrganisation(validGetSdltOrganisationPOSTRequest)

      status(result) shouldBe Status.OK
    }

    "return 400 when payload is invalid" in {
      val result = testController.getSdltOrganisation(invalidGetSdltOrganisationPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 404 when payload is valid but resource does not exist" in {
      val result = testController.getSdltOrganisation(nonExistentGetSdltOrganisationPOSTRequest)

      status(result) shouldBe Status.NOT_FOUND
    }
  }
}