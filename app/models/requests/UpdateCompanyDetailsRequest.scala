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

case class UpdateCompanyDetailsRequest(
                                        stornId: String,
                                        returnResourceRef: String,
                                        purchaserResourceRef: String,
                                        utr: Option[String] = None,
                                        vatReference: Option[String] = None,
                                        compTypeBank: Option[String] = None,
                                        compTypeBuilder: Option[String] = None,
                                        compTypeBuildsoc: Option[String] = None,
                                        compTypeCentgov: Option[String] = None,
                                        compTypeIndividual: Option[String] = None,
                                        compTypeInsurance: Option[String] = None,
                                        compTypeLocalauth: Option[String] = None,
                                        compTypeOcharity: Option[String] = None,
                                        compTypeOcompany: Option[String] = None,
                                        compTypeOfinancial: Option[String] = None,
                                        compTypePartship: Option[String] = None,
                                        compTypeProperty: Option[String] = None,
                                        compTypePubliccorp: Option[String] = None,
                                        compTypeSoletrader: Option[String] = None,
                                        compTypePenfund: Option[String] = None
                                      )

object UpdateCompanyDetailsRequest {
  implicit val format: OFormat[UpdateCompanyDetailsRequest] = Json.format[UpdateCompanyDetailsRequest]
}
