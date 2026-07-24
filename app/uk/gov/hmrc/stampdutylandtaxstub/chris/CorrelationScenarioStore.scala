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

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@Singleton
class CorrelationScenarioStore:

  private val byCorrelationId = new ConcurrentHashMap[String, String]()

  private def key(correlationId: String): String = correlationId.trim.toLowerCase

  def put(correlationId: String, returnResourceRef: String): Unit =
    if correlationId.trim.nonEmpty && returnResourceRef.trim.nonEmpty then
      byCorrelationId.put(key(correlationId), returnResourceRef.trim)

  def resourceRef(correlationId: String): Option[String] =
    Option(byCorrelationId.get(key(correlationId)))

  def remove(correlationId: String): Unit =
    byCorrelationId.remove(key(correlationId))