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

import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.xml.Elem

/** Decides what IRmark the success reply should carry.
  *
  *  - echo (default): copy the inbound //IRmark verbatim so the backend's
  *    UniversalStatus.irMarkMatches is true -> SUBMITTED.
  *  - recompute (optional): recompute from the received Body via IrMarkRecomputer,
  *    mirroring real ChRIS. Requires the santuario/xmlsec dependency.
  *
  * `mangled` deliberately returns a mark that will NOT match, to drive the
  * SUBMITTED_NO_RECEIPT branch regardless of mode.
  */
@Singleton
class IrMarkHandler @Inject() (config: ChrisStubConfig, recomputer: IrMarkRecomputer) extends Logging:

  /** The IRmark to place in a normal success response. */
  def markForSuccess(envelope: Elem): Option[String] =
    if config.recomputeIrMark then
      recomputer.recompute(envelope).orElse {
        logger.warn("[IrMarkHandler] recompute produced no mark; falling back to echo")
        echo(envelope)
      }
    else echo(envelope)

  /** The IRmark to place in a SUCCESS_NO_RECEIPT response — guaranteed mismatch. */
  def mangledMark(envelope: Elem): String =
    echo(envelope) match
      case Some(m) if m.nonEmpty =>
        val last    = m.last
        val flipped = if last == 'A' then 'B' else 'A'
        m.dropRight(1) + flipped
      case _ =>
        "NON_MATCHING_IRMARK"

  /** Extract the IRmark already present in the inbound envelope Body. */
  def echo(envelope: Elem): Option[String] =
    (envelope \\ "IRmark").map(_.text.trim).find(_.nonEmpty)
