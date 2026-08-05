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

package uk.gov.hmrc.stampdutylandtaxstub.controllers

import play.api.Logging
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.chris.*

import java.util.{Timer, TimerTask, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.xml.Elem

@Singleton
class ChrisStubController @Inject() (
                                      cc:      ControllerComponents,
                                      config:  ChrisStubConfig,
                                      builder: GovTalkResponseBuilder,
                                      store:   CorrelationScenarioStore
                                    )(implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging:

  private val timer: Timer = new Timer("chris-stub-timeout", true)

  def submit(): Action[String] = Action.async(parse.tolerantText) { implicit request =>
    val rawBody       = request.body
    val parsed        = parseEnvelope(rawBody)
    val function      = messageField(parsed, "Function").toLowerCase
    val clazz         = messageField(parsed, "Class")
    val correlationId = resolveCorrelationId(request.headers.get(ChrisStubConfig.CorrelationIdHeader), parsed)

    // Self-describing first: the backend injects <Key Type="ReturnResourceRef">
    // into the envelope's GovTalkDetails/Keys when talking to the stub. This works
    // regardless of replica or whether persistence went to the real formp-proxy.
    // Header and the legacy correlation-id store remain as fallbacks.
    val payloadRef = scenarioRefFromEnvelope(parsed)
    val storeRef   = store.resourceRef(correlationId)

    val scenario =
      Scenario.fromResourceRef(payloadRef)
        .orElse(Scenario.parseToken(request.headers.get(ChrisStubConfig.ScenarioHeader)))
        .orElse(Scenario.fromResourceRef(storeRef))
        .getOrElse(config.defaultScenario)

    val ctx = ChrisRequestContext(clazz, correlationId, parsed.getOrElse(<GovTalkMessage/>))

    logger.info(s"[ChrisStub] function=${if function.isEmpty then "<none>" else function} scenario=${Scenario.token(scenario)} payloadRef=${payloadRef.getOrElse("-")} storeRef=${storeRef.getOrElse("-")} corrId=$correlationId class=$clazz")

    val reply =
      if function == "delete" then
        store.remove(correlationId)
        builder.buildDelete(scenario, ctx)
      else builder.buildSubmit(scenario, ctx)

    render(reply)
  }

  private def render(reply: StubReply): Future[Result] = reply match
    case StubReply.Xml(status, envelope) =>
      Future.successful(Status(status)(envelope.toString).as("application/xml"))

    case StubReply.Raw(status, body) =>
      Future.successful(Status(status)(body).as("application/xml"))

    case StubReply.Delayed(delayMs, underlying) =>
      logger.info(s"[ChrisStub] delaying reply by ${delayMs}ms (TIMEOUT scenario)")
      after(delayMs).flatMap(_ => render(underlying))

  private def after(delayMs: Long): Future[Unit] =
    val p = Promise[Unit]()
    timer.schedule(new TimerTask { def run(): Unit = p.success(()) }, delayMs)
    p.future


  private def parseEnvelope(body: String): Option[Elem] =
    try Some(scala.xml.XML.loadString(body))
    catch
      case NonFatal(_) =>
        logger.warn("[ChrisStub] inbound body was not parseable XML; proceeding with header-only context")
        None

  private def messageField(parsed: Option[Elem], field: String): String =
    parsed
      .flatMap(xml => (xml \\ "MessageDetails" \ field).headOption)
      .map(_.text.trim)
      .getOrElse("")

  /** The resource reference the backend injected into GovTalkDetails/Keys as
   * `<Key Type="ReturnResourceRef">…</Key>`. Matched by label so the envelope's
   * default namespace doesn't matter. None if absent/blank.
   */
  private def scenarioRefFromEnvelope(parsed: Option[Elem]): Option[String] =
    parsed.flatMap { xml =>
      (xml \\ "Key")
        .find(k => (k \ "@Type").text.trim.equalsIgnoreCase("ReturnResourceRef"))
        .map(_.text.trim)
        .filter(_.nonEmpty)
    }

  private def resolveCorrelationId(header: Option[String], parsed: Option[Elem]): String =
    header.map(_.trim).filter(_.nonEmpty)
      .orElse(parsed.flatMap(xml => (xml \\ "CorrelationID").headOption.map(_.text.trim)).filter(_.nonEmpty))
      .getOrElse(UUID.randomUUID().toString.replace("-", "").toUpperCase)