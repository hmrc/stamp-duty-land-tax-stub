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

/** The set of ChRIS response behaviours the stub can produce.
 *
 * Selected by the `Gov-Test-Scenario` request header. Each value maps to one
 * spec-valid GovTalk reply (or transport behaviour). See GovTalkResponseBuilder
 * for the concrete reply per scenario.
 */
enum Scenario:
  case Success            // response/submit, UTRN + echoed IRmark        -> SUBMITTED
  case SuccessNoReceipt   // response/submit, UTRN + mangled IRmark       -> SUBMITTED_NO_RECEIPT
  case SuccessNoUtrn      // response/submit, no UTRN                     -> FATAL_ERROR (AF11)
  case Acknowledged       // acknowledgement + PollInterval               -> ACCEPTED
  case BusinessReject     // error Department/3001/business               -> DEPARTMENTAL_ERROR
  case Recoverable1000    // error Gateway/1000/fatal                     -> STARTED
  case Recoverable2005    // error Gateway/2005/fatal                     -> STARTED
  case Recoverable3000    // error Gateway/3000/fatal                     -> STARTED
  case SchemaError1001    // error Gateway/1001/fatal                     -> FATAL_ERROR
  case FatalOther         // error Gateway/1002/fatal                     -> FATAL_ERROR
  case MultiError         // several Error elements w/ Location            -> FATAL_ERROR + error details
  case HttpRetryable503   // HTTP 503 (no body)                           -> Errored(2005) -> STARTED
  case HttpFatal400       // HTTP 400 (no body)                           -> TransportError -> FATAL_ERROR
  case Timeout            // delayed past connector timeout               -> Errored(2005) -> STARTED
  case MalformedXml       // 200 with unparseable body                    -> TransportError -> FATAL_ERROR
  case DeleteNotFound     // (delete leg) error 2000                      -> NotFound

object Scenario:

  val Default: Scenario = Scenario.Success

  /** Canonical header token for each scenario. */
  def token(s: Scenario): String = s match
    case Scenario.Success          => "SUCCESS"
    case Scenario.SuccessNoReceipt => "SUCCESS_NO_RECEIPT"
    case Scenario.SuccessNoUtrn    => "SUCCESS_NO_UTRN"
    case Scenario.Acknowledged     => "ACKNOWLEDGED"
    case Scenario.BusinessReject   => "BUSINESS_REJECT"
    case Scenario.Recoverable1000  => "RECOVERABLE_1000"
    case Scenario.Recoverable2005  => "RECOVERABLE_2005"
    case Scenario.Recoverable3000  => "RECOVERABLE_3000"
    case Scenario.SchemaError1001  => "SCHEMA_ERROR_1001"
    case Scenario.FatalOther       => "FATAL_OTHER"
    case Scenario.MultiError       => "MULTI_ERROR"
    case Scenario.HttpRetryable503 => "HTTP_RETRYABLE_503"
    case Scenario.HttpFatal400     => "HTTP_FATAL_400"
    case Scenario.Timeout          => "TIMEOUT"
    case Scenario.MalformedXml     => "MALFORMED_XML"
    case Scenario.DeleteNotFound   => "DELETE_NOT_FOUND"

  private val byToken: Map[String, Scenario] =
    Scenario.values.map(s => token(s) -> s).toMap

  /** Friendly resource-reference aliases (legacy stub names) that aren't exact
   * scenario tokens. Tokens themselves (e.g. "BUSINESS_REJECT", "ACKNOWLEDGED",
   * "TIMEOUT") also work as resource refs via [[byToken]].
   */
  private val aliases: Map[String, Scenario] = Map(
    "SUBMITTED"   -> Scenario.Success,
    "REJECTED"    -> Scenario.BusinessReject,
    "NO_RECEIPT"  -> Scenario.SuccessNoReceipt,
    "NORECEIPT"   -> Scenario.SuccessNoReceipt,
    "FAILED"      -> Scenario.FatalOther,
    "ERROR"       -> Scenario.FatalOther,
    "RETRYABLE"   -> Scenario.Recoverable2005,
    // ChRIS must return a business reject so the backend enters its error branch
    // and calls createSubmissionErrorDetail — which this same ref then fails.
    "SUBMISSION_ERROR_DETAIL_ERROR" -> Scenario.BusinessReject
  )

  private def normalise(s: String): String = s.trim.toUpperCase.replace('-', '_')

  /** Parse an exact scenario token (used for the `Gov-Test-Scenario` header).
   * Lenient on case and `-`/`_`. Returns None for absent/blank/unknown.
   */
  def parseToken(value: Option[String]): Option[Scenario] =
    value.map(normalise).filter(_.nonEmpty).flatMap(byToken.get)

  /** Resolve a scenario from a return resource reference: an exact scenario token
   * OR a friendly alias. Returns None if it maps to neither.
   */
  def fromResourceRef(ref: Option[String]): Option[Scenario] =
    ref.map(normalise).filter(_.nonEmpty).flatMap(k => byToken.get(k).orElse(aliases.get(k)))

  /** Resolve a scenario from the raw `Gov-Test-Scenario` header value.
   * Absent, blank, or unknown values fall back to [[Default]].
   */
  def fromHeader(headerValue: Option[String], default: Scenario = Default): Scenario =
    parseToken(headerValue).getOrElse(default)