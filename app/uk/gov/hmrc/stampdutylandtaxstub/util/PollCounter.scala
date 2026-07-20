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

package uk.gov.hmrc.stampdutylandtaxstub.util

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@Singleton
class PollCounter {
  private val PollsBeforeTerminal = 10
  private val PollsBeforeTimeout  = 5
  private val counts = new ConcurrentHashMap[String, Int]()

  def reset(ref: String): Unit = counts.remove(ref)

  def resolve(ref: String, terminalStatus: String): String =
    if (ref == "stuck") "PENDING"
    else {
      val n = counts.merge(ref, 1, Integer.sum)
      if (n <= PollsBeforeTerminal) "PENDING" else terminalStatus
    }

  def isTimedOut(ref: String): Boolean =
    counts.merge(ref, 1, Integer.sum) > PollsBeforeTimeout
}