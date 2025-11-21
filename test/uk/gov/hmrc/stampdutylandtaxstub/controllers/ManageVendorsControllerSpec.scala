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

import models.requests.DeleteVendorRequest
import org.apache.pekko.actor.ActorSystem
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ManageVendorsControllerSpec
  extends AnyWordSpec
    with Matchers
    with GuiceOneServerPerSuite
    with MockitoSugar {

  implicit val system: ActorSystem = app.actorSystem

  private val validDeleteVendorRequest = DeleteVendorRequest(
    storn = "STORN12345",
    vendorResourceRef = "56789",
    returnResourceRef = "123456"
  )

  private val fakeDeleteVendorPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validDeleteVendorRequest))

  private val fakeDeleteVendorErrorPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(validDeleteVendorRequest.copy(returnResourceRef = "errorRemovingVendor")))

  private val invalidDeleteVendorPOSTRequest =
    FakeRequest("POST", "/")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.toJson(""))

  lazy val testController: ManageVendorsController = app.injector.instanceOf[ManageVendorsController]

  ".removeVendor" should {

    "return 200 when payload is valid and resource exists" in {
      val result = testController.removeVendor()(fakeDeleteVendorPOSTRequest)

      status(result) shouldBe Status.OK
    }

    "return 400 when payload is valid and returnResourceRef is errorRemovingVendor" in {
      val result = testController.removeVendor()(fakeDeleteVendorErrorPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "return 400 when payload is invalid" in {
      val result = testController.removeVendor()(invalidDeleteVendorPOSTRequest)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }

    "return 400 when required fields are missing" in {
      val invalidRequest = Json.obj("returnResourceRef" -> "123456")
      val request = FakeRequest("POST", "/")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(invalidRequest)

      val result = testController.removeVendor()(request)

      status(result) shouldBe Status.BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] should include("Invalid payload")
    }
  }
}