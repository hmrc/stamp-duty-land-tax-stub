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

package uk.gov.hmrc.stampdutylandtaxstub.chris

import scala.xml.Elem

/** What the stub intends to send back. The controller turns this into a Play
  * Result (and applies the delay, if any).
  */
enum StubReply:
  /** A well-formed GovTalk XML reply at the given HTTP status. */
  case Xml(status: Int, envelope: Elem)
  /** A raw body (e.g. deliberately malformed XML) at the given HTTP status. */
  case Raw(status: Int, body: String)
  /** Wrap another reply with a pre-send delay (TIMEOUT scenario). */
  case Delayed(delayMs: Long, underlying: StubReply)
