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

import play.api.Configuration

import javax.inject.{Inject, Singleton}

/** Typed view over the `stub.chris.*` config block. */
@Singleton
class ChrisStubConfig @Inject() (configuration: Configuration):

  private val root = configuration.get[Configuration]("stub.chris")

  val irMarkMode: String       = root.getOptional[String]("irmark.mode").getOrElse("echo").trim.toLowerCase
  val pollInterval: Int        = root.getOptional[Int]("response.poll-interval").getOrElse(10)
  val responseEndpoint: String = root.getOptional[String]("response.endpoint").getOrElse("http://localhost:9000/ChRIS/SDLT/Filing/sync/SDLT")
  val timeoutDelayMs: Long     = root.getOptional[Long]("timeout.delay-ms").getOrElse(5000L)
  val utrnSuffixLetters: String =
    root.getOptional[String]("utrn.suffix-letters").getOrElse("ABCDEFGHJKLMNPQRSTVWXYZ")

  val defaultScenario: Scenario =
    Scenario.fromHeader(root.getOptional[String]("scenario.default"))

  val recomputeIrMark: Boolean = irMarkMode == "recompute"

  // formp-proxy prerequisite toggles — let testers deterministically drive the
  // two SubmissionService branches that a plain happy-path stub never exercises.
  // (Kept as config rather than Gov-Test-Scenario because FilingFormpProxyConnector
  //  does not forward that header unless you use MDTP allowlist propagation.)
  val persistenceLockConflict: Boolean =
    root.getOptional[Boolean]("persistence.lock-conflict").getOrElse(false)
  val persistenceGovTalkRowExists: Boolean =
    root.getOptional[Boolean]("persistence.govtalk-row-exists").getOrElse(false)

  // Per-endpoint fault injection. Any key set to true under
  // stub.chris.persistence.faults makes that formp-proxy endpoint return 500,
  // so you can exercise the backend's handling of a failed persistence call.
  // Orthogonal to the ChRIS scenario, so faults combine with any resource ref.
  private val enabledFaults: Set[String] =
    root.getOptional[Configuration]("persistence.faults")
      .map(c => c.keys.filter(k => c.getOptional[Boolean](k).getOrElse(false)))
      .getOrElse(Set.empty)

  def faultEnabled(name: String): Boolean = enabledFaults.contains(name)

object ChrisStubConfig:
  /** The header testers set to choose a scenario. */
  val ScenarioHeader: String = "Gov-Test-Scenario"
  /** The header ChrisConnector sends the correlation id on. */
  val CorrelationIdHeader: String = "CorrelationId"