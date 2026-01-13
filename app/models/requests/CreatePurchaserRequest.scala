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

package models.requests

import play.api.libs.json.{Json, OFormat}

case class CreatePurchaserRequest(
                                   stornId: String,
                                   returnResourceRef: String,
                                   isCompany: String,
                                   isTrustee: String,
                                   isConnectedToVendor: String,
                                   isRepresentedByAgent: String,
                                   title: Option[String] = None,
                                   surname: Option[String] = None,
                                   forename1: Option[String] = None,
                                   forename2: Option[String] = None,
                                   companyName: Option[String] = None,
                                   houseNumber: Option[String] = None,
                                   address1: String,
                                   address2: Option[String] = None,
                                   address3: Option[String] = None,
                                   address4: Option[String] = None,
                                   postcode: Option[String] = None,
                                   phone: Option[String] = None,
                                   nino: Option[String] = None,
                                   isUkCompany: Option[String] = None,
                                   hasNino: Option[String] = None,
                                   dateOfBirth: Option[String] = None,
                                   registrationNumber: Option[String] = None,
                                   placeOfRegistration: Option[String] = None
                                 )

object CreatePurchaserRequest {
  implicit val format: OFormat[CreatePurchaserRequest] = Json.format[CreatePurchaserRequest]
}