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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}

class PrelimReturnControllerSpec
  extends AnyWordSpec
     with Matchers with GuiceOneServerPerSuite with MockitoSugar :

  private val fakeRequest = FakeRequest("GET", "/")
  lazy val testController: PrelimReturnController = app.injector.instanceOf[PrelimReturnController]
  val testJson: JsValue = Json.parse(
    """{
      "stornId": "12435",
      "purchaserIsCompany": "YES",
      "surNameOrCompanyName": "Test Name",
      "houseNumber": 23,
      "addressLine1": "Test road name",
      "addressLine2": null,
      "addressLine3": null,
      "addressLine4": null,
      "postcode": "TE23 5TT",
      "transactionType": "O"
    }"""
  )

  "GET /" should:
    "return 404 when no return id is found" in:
      val result = testController.prelimReturnDetails(None)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND

    "return 404 when invalid return id is sent" in :
      val result = testController.prelimReturnDetails(Some("55555"))(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND

    "return 200 when a valid return id has been found" in :
      val result = testController.prelimReturnDetails(Some("123456"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe testJson

