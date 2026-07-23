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

import javax.inject.{Inject, Singleton}
import scala.util.Random

/** Generates UTRNs that satisfy the connector's validation pattern:
  *   ^[0-9]{9}M[A-HJ-NP-TV-Z]$
  *
  * Nine digits, a literal 'M', then a check letter from an alphabet that
  * excludes I, O and U. ChrisConnector only regex-validates the UTRN — it does
  * not verify a check-letter algorithm — so any letter from the allowed set is
  * accepted downstream.
  */
@Singleton
class UtrnGenerator @Inject() (config: ChrisStubConfig):

  private val letters: String = config.utrnSuffixLetters
  private val rnd: Random     = new Random()

  def generate(): String =
    val digits = Array.fill(9)(rnd.nextInt(10)).mkString
    val letter = letters.charAt(rnd.nextInt(letters.length))
    s"${digits}M$letter"
