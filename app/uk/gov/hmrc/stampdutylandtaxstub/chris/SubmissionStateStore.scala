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

import play.api.libs.json.{JsObject, Json}

import java.time.{Duration, Instant}
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

/** Minimal in-memory submission state, keyed by return resource reference.
 *
 * The backend writes the submission's progress via `updateSubmission`; the stub
 * records the latest view here so `getFullReturn` can reflect it — making the
 * "return is being submitted" poll complete once the backend has written a
 * terminal status. Cleared at the start of a fresh submit (`createSubmission`).
 *
 * Entries expire one minute after their last write, so a completed submission
 * drops back to the fixture's pre-submit state and the journey can be re-run
 * without a manual reset or restart.
 *
 * In-memory only: a stub restart resets everything.
 */
@Singleton
class SubmissionStateStore:

  private val Ttl = Duration.ofMinutes(30)

  private final case class Entry(submission: JsObject, writtenAt: Instant)

  private val byRef = new ConcurrentHashMap[String, Entry]()

  protected def now(): Instant = Instant.now()

  private def key(ref: String): String = ref.trim

  private def expired(e: Entry): Boolean = e.writtenAt.plus(Ttl).isBefore(now())

  /** Merge new fields into the stored submission view for this ref.
   * An expired entry is replaced rather than merged into. */
  def merge(ref: String, submission: JsObject): Unit =
    if ref.trim.nonEmpty then
      byRef.compute(
        key(ref),
        (_, existing) =>
          if existing == null || expired(existing) then Entry(submission, now())
          else Entry(existing.submission ++ submission, now())
      )

  def seedSubmissionId(ref: String, submissionId: String): Unit =
    if ref.trim.nonEmpty && submissionId.trim.nonEmpty then
      merge(ref, Json.obj("submissionID" -> submissionId.trim))

  /** The stored view, or None if absent or expired (expired entries are evicted). */
  def get(ref: String): Option[JsObject] =
    Option(byRef.get(key(ref))) match
      case Some(e) if expired(e) =>
        byRef.remove(key(ref), e)
        None
      case other =>
        other.map(_.submission)

  def clear(ref: String): Unit = byRef.remove(key(ref))