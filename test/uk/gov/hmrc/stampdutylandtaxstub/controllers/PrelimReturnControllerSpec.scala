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

import models.requests.{GetReturnByRefRequest, PrelimReturn}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import play.api.test.FakeRequest

class PrelimReturnControllerSpec
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

  lazy val testController: PrelimReturnController = app.injector.instanceOf[PrelimReturnController]

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
}