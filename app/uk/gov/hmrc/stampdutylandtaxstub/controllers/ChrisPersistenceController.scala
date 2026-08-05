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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.stampdutylandtaxstub.chris.{ChrisStubConfig, CorrelationScenarioStore, SubmissionStateStore}

import javax.inject.{Inject, Singleton}

/** formp-proxy prerequisite endpoints, with fault injection driven by the
 * return's resource reference (like the ChRIS scenarios) or by config.
 *
 * Each endpoint receives the resource reference in its own body
 * (`formResultId` / `returnResourceRef` / `resultId`). Name a test return after
 * a fault token and only the matching endpoint 500s — every other endpoint sees
 * the same ref, doesn't recognise it as ITS fault, and succeeds. e.g. a return
 * whose ref is `govtalk-lock-error` fails only `/filing/update/govtalk-status/lock`.
 *
 * Matching is case-insensitive and treats `-`/`_` alike. The equivalent config
 * key under `stub.chris.persistence.faults` also triggers the fault (OR), which is
 * how you combine a fault with a different ChRIS ref (e.g. `rejected` return +
 * `faults.submission-error-detail = true`). See EVERY-OUTCOME.md.
 */
@Singleton
class ChrisPersistenceController @Inject() (
                                             cc:              ControllerComponents,
                                             config:          ChrisStubConfig,
                                             store:           CorrelationScenarioStore,
                                             submissionState: SubmissionStateStore
                                           ) extends BackendController(cc) with Logging:

  /** Map the backend's SubmissionUpdate fields onto the FullReturn `Submission`
   * field names that getFullReturn / the FE read. Only the present fields.
   *
   * IMPORTANT: SubmissionCompleteController renders only when it can read
   * UTRN + submissionRequestDate (+ a tax total from taxCalculation); if any is
   * missing it redirects to the task list, which (seeing a submission present)
   * redirects to before-you-start, which (SUBMITTED) redirects back to complete
   * — an infinite loop. So every field the completion/failed pages read must be
   * carried through here, not just the status.
   */
  private def submissionView(sub: JsObject): JsObject =
    def map(from: String, to: String): Option[(String, Json.JsValueWrapper)] =
      (sub \ from).asOpt[String].map(v => to -> Json.toJsFieldJsValueWrapper(v))
    Json.obj(Seq(
      map("submittableStatus",     "submissionStatus"),
      map("utrn",                  "UTRN"),
      map("IRMarkRecieved",        "irmarkReceived"),
      map("IRMarkSent",            "irmarkSent"),
      map("email",                 "email"),
      map("submissionRequestDate", "submissionRequestDate"),
      map("acceptedDate",          "acceptedDate"),
      map("govTalkErrorCode",      "govtalkErrorCode"),
      map("govTalkErrorType",      "govtalkErrorType"),
      map("govTalkErrorMessage",   "govtalkErrorMessage")
    ).flatten*)

  private def normalise(s: String): String = s.trim.toUpperCase.replace('-', '_')

  /** The resource reference, wherever it lives on this request body. */
  private def refOf(body: JsValue): Option[String] =
    (body \ "formResultId").asOpt[String]
      .orElse((body \ "returnResourceRef").asOpt[String])
      .orElse((body \ "resultId").asOpt[String])
      .map(_.trim).filter(_.nonEmpty)

  /** True when this endpoint's fault is triggered — by the ref token or config. */
  private def faultTriggered(body: JsValue, configKey: String, refToken: String): Boolean =
    config.faultEnabled(configKey) || refOf(body).map(normalise).contains(refToken)

  // Refs that make selectGovTalkStatus report an existing row (-> reset path),
  // so the reset path is driven purely by the resource reference (no config).
  private val rowExistsRefs: Set[String] = Set("GOVTALK_ROW_EXISTS", "GOVTALK_RESET_ERROR")

  private def wantsExistingRow(body: JsValue): Boolean =
    config.persistenceGovTalkRowExists || refOf(body).map(normalise).exists(rowExistsRefs.contains)

  private def fault(op: String, trigger: String): Result =
    logger.warn(s"[ChrisPersistence][$op] INJECTED FAULT ($trigger) -> 500")
    InternalServerError(Json.obj("statusCode" -> 500, "message" -> s"Injected persistence fault: $trigger"))

  private def okJson: Result = Ok(Json.obj("success" -> true))

  private def successSubmissionJson(submissionId: String): Result =
    Ok(Json.obj("success" -> true, "submissionId" -> submissionId))

  /** Success unless this endpoint's fault (ref token or config key) is triggered. */
  private def faulted(op: String, configKey: String, refToken: String): Action[JsValue] = Action(parse.json) { request =>
    logger.info(s"[ChrisPersistence][$op] ${request.body}")
    if faultTriggered(request.body, configKey, refToken) then fault(op, refToken) else okJson
  }

  // Record correlationId -> returnResourceRef so the ChRIS controller can pick a
  // scenario from the resource reference. Both fields are present on this request.
  private def capture(body: JsValue): Unit =
    (body \ "correlationId").asOpt[String].zip((body \ "formResultId").asOpt[String])
      .foreach { case (corr, ref) =>
        store.put(corr, ref)
        logger.info(s"[ChrisPersistence] mapped correlationId=$corr -> returnResourceRef=$ref")
      }
  
  def lockReturn(): Action[JsValue] = Action(parse.json) { request =>
    logger.info(s"[ChrisPersistence][lockReturn] ${request.body}")
    if config.persistenceLockConflict || faultTriggered(request.body, "lock", "LOCK_ERROR") then
      Conflict(Json.obj("statusCode" -> 409, "message" -> "Return is locked by another submission"))
    else okJson
  }


  def createSubmission(): Action[JsValue] = Action(parse.json) { request =>
    logger.info(s"[ChrisPersistence][createSubmission] ${request.body}")
    if faultTriggered(request.body, "create-submission", "CREATE_SUBMISSION_ERROR") then
      fault("createSubmission", "CREATE_SUBMISSION_ERROR")
    else
      val ref = (request.body \ "returnResourceRef").asOpt[String].map(_.trim).filter(_.nonEmpty)
      val id  = ref.getOrElse("STUB-SUBMISSION")
      ref.foreach { r =>
        submissionState.clear(r)          
        submissionState.seedSubmissionId(r, id) 
      }
      successSubmissionJson(id)
  }

  def updateSubmission(): Action[JsValue] = Action(parse.json) { request =>
    logger.info(s"[ChrisPersistence][updateSubmission] ${request.body}")
    if faultTriggered(request.body, "update-submission", "UPDATE_SUBMISSION_ERROR") then
      fault("updateSubmission", "UPDATE_SUBMISSION_ERROR")
    else
      for
        ref <- (request.body \ "returnResourceRef").asOpt[String]
        sub <- (request.body \ "submission").asOpt[JsObject]
      do submissionState.merge(ref, submissionView(sub))
      okJson
  }

  def createSubmissionErrorDetail(): Action[JsValue] = faulted("createSubmissionErrorDetail", "submission-error-detail", "SUBMISSION_ERROR_DETAIL_ERROR")
  def deleteSubmissionErrorDetail(): Action[JsValue] = faulted("deleteSubmissionErrorDetail", "delete-error-detail", "DELETE_ERROR_DETAIL_ERROR")

  // --- GovTalk status writes ------------------------------------------------
  // insertInitialGovTalkStatus carries BOTH correlationId and formResultId — the
  // join point that lets the ChRIS controller resolve a scenario from the ref.
  // ref token: govtalk-insert-error
  def insertInitialGovTalkStatus(): Action[JsValue] = Action(parse.json) { request =>
    logger.info(s"[ChrisPersistence][insertInitialGovTalkStatus] ${request.body}")
    capture(request.body)
    if faultTriggered(request.body, "govtalk-insert", "GOVTALK_INSERT_ERROR") then
      fault("insertInitialGovTalkStatus", "GOVTALK_INSERT_ERROR")
    else okJson
  }

  def resetGovTalkStatus(): Action[JsValue] = faulted("resetGovTalkStatus", "govtalk-reset", "GOVTALK_RESET_ERROR")
  def updateGovTalkStatus(): Action[JsValue] = faulted("updateGovTalkStatus", "govtalk-update", "GOVTALK_UPDATE_ERROR")

  def updateGovTalkStatusCorrelationId(): Action[JsValue] = Action(parse.json) { request =>
    logger.info(s"[ChrisPersistence][updateGovTalkStatusCorrelationId] ${request.body}")
    capture(request.body)
    if faultTriggered(request.body, "govtalk-correlation-id", "GOVTALK_CORRELATION_ID_ERROR") then
      fault("updateGovTalkStatusCorrelationId", "GOVTALK_CORRELATION_ID_ERROR")
    else okJson
  }

  def updateGovTalkStatusLock(): Action[JsValue] = faulted("updateGovTalkStatusLock", "govtalk-lock", "GOVTALK_LOCK_ERROR")
  def updateGovTalkStatistics(): Action[JsValue] = faulted("updateGovTalkStatistics", "govtalk-statistics", "GOVTALK_STATISTICS_ERROR")
  def deleteGovTalkStatus(): Action[JsValue]     = faulted("deleteGovTalkStatus", "govtalk-delete", "GOVTALK_DELETE_ERROR")

  // --- GovTalk status reads -------------------------------------------------
  //   default             -> empty object => "no row" => insert-initial path
  //   govtalk-row-exists   -> a populated row => reset path + stored gatewayUrl
  //   ref govtalk-select-error / faults.govtalk-select -> 500; NOTE the backend
  //     connector RECOVERS this to "no row", so it does NOT fail the submit.
  def selectGovTalkStatus(): Action[AnyContent] = Action { request =>
    val body = request.body.asJson.getOrElse(Json.obj())
    logger.info(s"[ChrisPersistence][selectGovTalkStatus] $body")
    if faultTriggered(body, "govtalk-select", "GOVTALK_SELECT_ERROR") then
      fault("selectGovTalkStatus", "GOVTALK_SELECT_ERROR")
    else if wantsExistingRow(body) then
      Ok(Json.obj(
        "userIdentifier"       -> "STUB-STORN",
        "formResultId"         -> "STUB-FORM-RESULT",
        "correlationId"        -> "STUBCORRELATIONID",
        "formLock"             -> "N",
        "createTimestamp"      -> "2026-01-01 00:00:00.000",
        "endStateTimestamp"    -> "2026-01-01 00:00:00.000",
        "lastMessageTimestamp" -> "2026-01-01 00:00:00.000",
        "numberOfPolls"        -> "0",
        "pollInterval"         -> "0",
        "protocolStatus"       -> "submit",           // non-empty => triggers reset
        "gatewayUrl"           -> config.responseEndpoint
      ))
    else
      Ok(Json.obj())                                   // no row
  }

  // Not on the sync submit path in the connector provided.
  def selectGovTalkFormResultId(): Action[AnyContent] = Action { request =>
    logger.info(s"[ChrisPersistence][selectGovTalkFormResultId] ${request.body.asText.getOrElse("")}")
    Ok(Json.obj())
  }