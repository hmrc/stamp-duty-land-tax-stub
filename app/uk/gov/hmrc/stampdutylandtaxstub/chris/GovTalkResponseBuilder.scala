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
import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.xml.{Elem, NodeSeq}

/** Everything the templates need from the inbound request. */
final case class ChrisRequestContext(
  clazz:         String,   // echoed MessageDetails/Class (SDLT = IR-SDLT-LTR)
  correlationId: String,   // echoed CorrelationID
  envelope:      Elem      // the parsed inbound GovTalk envelope
):
  def classOrDefault: String = if clazz.nonEmpty then clazz else "IR-SDLT-LTR"

/** Renders a [[StubReply]] for a scenario, echoing request-derived values and
  * injecting a generated UTRN / IRmark / timestamp where required.
  *
  * Stateless and thread-safe: every value the templates need is passed as a
  * method parameter.
  */
@Singleton
class GovTalkResponseBuilder @Inject() (
  config:        ChrisStubConfig,
  utrnGenerator: UtrnGenerator,
  irMarkHandler: IrMarkHandler
):

  private val timestampFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

  private def now(): String = ZonedDateTime.now(ZoneOffset.UTC).format(timestampFmt)

  // ----- submit leg ---------------------------------------------------------

  def buildSubmit(scenario: Scenario, ctx: ChrisRequestContext): StubReply =
    import Scenario.*
    scenario match
      case Success =>
        StubReply.Xml(200, successEnvelope(ctx, Some(utrnGenerator.generate()), irMarkHandler.markForSuccess(ctx.envelope)))

      case SuccessNoReceipt =>
        StubReply.Xml(200, successEnvelope(ctx, Some(utrnGenerator.generate()), Some(irMarkHandler.mangledMark(ctx.envelope))))

      case SuccessNoUtrn =>
        StubReply.Xml(200, successEnvelope(ctx, None, irMarkHandler.markForSuccess(ctx.envelope)))

      case Acknowledged =>
        StubReply.Xml(200, acknowledgementEnvelope(ctx))

      case BusinessReject =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Department", "3001", "business",
            "The submission of this document has failed due to departmental specific business logic in the Body element.",
            Some("/hd:GovTalkMessage[1]/hd:Body[1]")))))

      case Recoverable1000 =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Gateway", "1000", "fatal",
            "System failure. The submission of this document has failed due to an internal system error.", None))))

      case Recoverable2005 =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Gateway", "2005", "fatal",
            "The Transaction Engine has not received an acknowledgement of your submission within the permitted timescale. Either resubmit or contact the appropriate organisation directly.", None))))

      case Recoverable3000 =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Gateway", "3000", "fatal",
            "The processing of your document submission failed. Please re-submit.", None))))

      case SchemaError1001 =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Gateway", "1001", "fatal",
            "The submitted XML document either failed to validate against the GovTalk schema for this class of document or its body was badly formed.", None))))

      case FatalOther =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Gateway", "1002", "fatal", "Authentication Failure.", None))))

      case MultiError =>
        StubReply.Xml(200, errorEnvelope(ctx, "submit", Seq(
          GovTalkErrorXml("Gateway", "1001", "fatal", "Element failed schema validation.", Some("/hd:GovTalkMessage[1]/hd:Body[1]/sdlt:purchaser[1]")),
          GovTalkErrorXml("Gateway", "1040", "fatal", "The submitted document contains an inconsistent value entry for the specified method.", Some("/hd:GovTalkMessage[1]/hd:Body[1]/sdlt:transaction[1]")))))

      case HttpRetryable503 =>
        StubReply.Raw(503, "")

      case HttpFatal400 =>
        StubReply.Raw(400, "")

      case Timeout =>
        StubReply.Delayed(config.timeoutDelayMs,
          StubReply.Xml(200, successEnvelope(ctx, Some(utrnGenerator.generate()), irMarkHandler.markForSuccess(ctx.envelope))))

      case MalformedXml =>
        StubReply.Raw(200, "this is not xml <<< GovTalkMessage")

      case DeleteNotFound =>
        // Not a submit scenario — behave as SUCCESS if it lands here.
        StubReply.Xml(200, successEnvelope(ctx, Some(utrnGenerator.generate()), irMarkHandler.markForSuccess(ctx.envelope)))

  // ----- delete leg ---------------------------------------------------------

  def buildDelete(scenario: Scenario, ctx: ChrisRequestContext): StubReply =
    scenario match
      case Scenario.DeleteNotFound =>
        StubReply.Xml(200, errorEnvelope(ctx, "delete", Seq(
          GovTalkErrorXml("Gateway", "2000", "fatal",
            "The Transaction Engine could not locate a record for the supplied correlation ID.", None))))

      case _ =>
        StubReply.Xml(200, deleteResponseEnvelope(ctx))

  // ----- templates ----------------------------------------------------------

  private def messageDetails(clazz: String, correlationId: String, qualifier: String, function: String, withEndpoint: Boolean): Elem =
    <MessageDetails>
      <Class>{clazz}</Class>
      <Qualifier>{qualifier}</Qualifier>
      <Function>{function}</Function>
      <TransactionID></TransactionID>
      <CorrelationID>{correlationId}</CorrelationID>
      {if withEndpoint then <ResponseEndPoint PollInterval={config.pollInterval.toString}>{config.responseEndpoint}</ResponseEndPoint> else NodeSeq.Empty}
      <Transformation>XML</Transformation>
      <GatewayTimestamp>{now()}</GatewayTimestamp>
    </MessageDetails>

  private def successEnvelope(ctx: ChrisRequestContext, utrn: Option[String], irMark: Option[String]): Elem =
    <GovTalkMessage xmlns="http://www.govtalk.gov.uk/CM/envelope">
      <EnvelopeVersion>2.0</EnvelopeVersion>
      <Header>
        {messageDetails(ctx.classOrDefault, ctx.correlationId, "response", "submit", withEndpoint = true)}
        <SenderDetails/>
      </Header>
      <GovTalkDetails><Keys/></GovTalkDetails>
      <Body>
        <SuccessResponse xmlns="http://www.govtalk.gov.uk/taxation/SDLT/1">
          {irMark.map(irMarkReceipt).getOrElse(NodeSeq.Empty)}
          {utrn.map(u => <UTRN>{u}</UTRN>).getOrElse(NodeSeq.Empty)}
        </SuccessResponse>
      </Body>
    </GovTalkMessage>

  private def irMarkReceipt(mark: String): Elem =
    <IRmarkReceipt>
      <dsig:Signature xmlns:dsig="http://www.w3.org/2000/09/xmldsig#">
        <dsig:SignedInfo>
          <dsig:Reference>
            <dsig:DigestValue>{mark}</dsig:DigestValue>
          </dsig:Reference>
        </dsig:SignedInfo>
      </dsig:Signature>
    </IRmarkReceipt>

  private def acknowledgementEnvelope(ctx: ChrisRequestContext): Elem =
    <GovTalkMessage xmlns="http://www.govtalk.gov.uk/CM/envelope">
      <EnvelopeVersion>2.0</EnvelopeVersion>
      <Header>
        {messageDetails(ctx.classOrDefault, ctx.correlationId, "acknowledgement", "submit", withEndpoint = true)}
        <SenderDetails/>
      </Header>
      <GovTalkDetails><Keys/></GovTalkDetails>
      <Body/>
    </GovTalkMessage>

  private def deleteResponseEnvelope(ctx: ChrisRequestContext): Elem =
    <GovTalkMessage xmlns="http://www.govtalk.gov.uk/CM/envelope">
      <EnvelopeVersion>2.0</EnvelopeVersion>
      <Header>
        {messageDetails(ctx.classOrDefault, ctx.correlationId, "response", "delete", withEndpoint = true)}
        <SenderDetails/>
      </Header>
      <GovTalkDetails><Keys/></GovTalkDetails>
      <Body/>
    </GovTalkMessage>

  private def errorEnvelope(ctx: ChrisRequestContext, function: String, errors: Seq[GovTalkErrorXml]): Elem =
    <GovTalkMessage xmlns="http://www.govtalk.gov.uk/CM/envelope">
      <EnvelopeVersion>2.0</EnvelopeVersion>
      <Header>
        {messageDetails(ctx.classOrDefault, ctx.correlationId, "error", function, withEndpoint = true)}
        <SenderDetails/>
      </Header>
      <GovTalkDetails>
        <Keys/>
        <GovTalkErrors>
          {errors.map(_.toXml)}
        </GovTalkErrors>
      </GovTalkDetails>
      <Body/>
    </GovTalkMessage>

/** Small holder for a GovTalk Error element. */
final case class GovTalkErrorXml(
  raisedBy:  String,
  number:    String,
  errorType: String,
  text:      String,
  location:  Option[String]
):
  def toXml: Elem =
    <Error>
      <RaisedBy>{raisedBy}</RaisedBy>
      <Number>{number}</Number>
      <Type>{errorType}</Type>
      <Text>{text}</Text>
      {location.map(l => <Location>{l}</Location>).getOrElse(scala.xml.NodeSeq.Empty)}
    </Error>
