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

case class ResidencyPayload(
  isNonUkResidents: String,
  isCompany: String,
  isCrownRelief: String
)

object ResidencyPayload {
  implicit val format: OFormat[ResidencyPayload] = Json.format[ResidencyPayload]
}

case class CreateResidencyRequest(
  stornId: String,
  returnResourceRef: String,
  residency: ResidencyPayload
)

object CreateResidencyRequest {
  implicit val format: OFormat[CreateResidencyRequest] = Json.format[CreateResidencyRequest]
}
