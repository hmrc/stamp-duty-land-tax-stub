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

import org.apache.xml.security.Init
import org.apache.xml.security.signature.XMLSignatureInput
import org.apache.xml.security.transforms.Transforms
import org.apache.xml.security.transforms.params.XPathContainer
import org.w3c.dom.Document
import play.api.Logging

import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import scala.xml.Elem

/** Recomputes an IRmark from a received GovTalk envelope using the SAME
  * algorithm as the backend's service.submission.IrMarkService:
  * XPath transform over Body (excluding the IRmark element) ->
  * C14N-with-comments -> SHA-1 -> Base64.
  *
  * Because the XPath excludes the IRmark element subtree, the digest is
  * independent of the mark value already spliced in by the backend, so this
  * reproduces the same value the backend computed — i.e. it VALIDATES it.
  *
  * DEPENDENCY: requires santuario/xmlsec on the stub classpath, e.g.
  *   "org.apache.santuario" % "xmlsec" % "<version matching the backend>"
  * If you only ever use irmark.mode = "echo", you can delete this file and the
  * recompute branch in IrMarkHandler, and drop the dependency.
  */
@Singleton
class IrMarkRecomputer extends Logging:

  Init.init()

  private val GovTalkEnvelopeNs = "http://www.govtalk.gov.uk/CM/envelope"

  private val IrMarkXPath: String =
    "(count(ancestor-or-self::node()|/gt:GovTalkMessage/gt:Body)=count(ancestor-or-self::node()))" +
      " and " +
      "(count(ancestor-or-self::node()|/gt:GovTalkMessage/gt:Body/*[name()='IRenvelope']/*[name()='IRheader']/*[name()='IRmark'])!=count(ancestor-or-self::node()))"

  private val dbf: DocumentBuilderFactory =
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(true)
    factory

  def recompute(envelope: Elem): Option[String] =
    try
      val doc = elemToDom(envelope)

      val transforms = new Transforms(doc)
      val xpath      = new XPathContainer(doc)
      xpath.setXPathNamespaceContext("gt", GovTalkEnvelopeNs)
      xpath.setXPath(IrMarkXPath)
      transforms.addTransform(Transforms.TRANSFORM_XPATH, xpath.getElement)
      transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS)

      val output    = transforms.performTransforms(new XMLSignatureInput(doc))
      val canonical = output.getBytes

      val digest = MessageDigest.getInstance("SHA-1").digest(canonical)
      Some(Base64.getEncoder.encodeToString(digest))
    catch
      case e: Throwable =>
        logger.error("[IrMarkRecomputer] recompute failed", e)
        None

  private def elemToDom(elem: Elem): Document =
    dbf.newDocumentBuilder().parse(new ByteArrayInputStream(elem.toString.getBytes("UTF-8")))
