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

package uk.gov.hmrc.stampdutylandtaxstub.sql

import uk.gov.hmrc.stampdutylandtaxstub.sql.Tables.*
import uk.gov.hmrc.stampdutylandtaxstub.sql.Tables.profile.api.*

import java.sql.Timestamp
import java.time.Instant
import scala.language.postfixOps

object InsertQueries {
  import DataGenerator._

  // ORGANISATION
  val insertOrgAction = (storn: String) =>
    DBIO
      .seq(
        Tables.SdltOrganisation += SdltOrganisationRow(
          storn = storn,
          doNotDisplayWelcomePage = None,
          isReturnUser = None,
          agentCounter = None,
          returnCounter = None,
          version = None,
          lMigrated = None,
          createDate = java.sql.Timestamp.from(Instant.now()),
          lastUpdateDate = java.sql.Timestamp.from(Instant.now())
        )
      )
      .transactionally

  // RETURNS
  val multipleReturnRows = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    (1 to recNumber)
      .map(id =>
        ReturnRow(
          returnId = BigDecimal(nextId.nextReturnId + id),
          storn = storn,
          purchaserCounter = BigDecimal(1),
          vendorCounter = BigDecimal(1),
          landCounter = BigDecimal(1),
          version = None,
          mainPurchaserId = None,
          mainVendorId = None,
          mainLandId = None,
          irmarkGenerated = None,
          landCertForEachProp = None,
          purgeDate = returnType match {
            case DueForDeletionReturns =>
              Some(java.sql.Timestamp.from(Instant.now()))
            case _                     =>
              None
          },
          returnResourceRef = Some(BigDecimal(nextId.nextReturnId + id)),
          status = returnType match {
            case InProgressReturns     =>
              "STARTED"
            case SubmissionReturns     =>
              "SUBMITTED"
            case DueForDeletionReturns =>
              "SUBMITTED" // (''PENDING'',''ACCEPTED'',''STARTED'')'
          },
          lMigrated = None,
          createDate = Timestamp(0),
          lastUpdateDate = Timestamp(0),
          declaration = None
        )
      )
      .toList


  val maxReturnIdQuery = Tables.Return.map(_.returnId).max.result
  val maxReturnAgentIdQuery = Tables.ReturnAgent.map(_.returnAgentId).max.result
  val maxLandIdQuery = Tables.Land.map(_.landId).max.result
  val maxPurchaserIdQuery = Tables.Purchaser.map(_.purchaserId).max.result
  val maxSubmissionIdQuery = Tables.Submission.map(_.submissionId).max.result

  case class NextId(nextReturnId: Int,
                    nextReturnAgentId: Int,
                    nextLandId: Int,
                    nextPurchaserId: Int,
                    nextSubmissionId: Int)

  val insertReturnAction = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    DBIO
      .seq(
        Tables.Return ++= multipleReturnRows(recNumber, storn, returnType, nextId)
      )
      .transactionally

  // AGENT_RETURNS
  val multipleAgentReturns = (recNumber: Int, returnType: ReturnType, nextId: NextId) =>
    (1 to recNumber).map(id =>
      ReturnAgentRow(
        returnAgentId = BigDecimal(nextId.nextReturnAgentId + id),
        returnId = Some(BigDecimal(nextId.nextReturnId + id)),
        agentType = "PURCHASER",
        name = Some("FoxAgencyy"),
        houseNumber = Some("num 18"),
        address1 = Some("Address Line 777" + id),
        address2 = None,
        address3 = None,
        address4 = None,
        postcode = Some("SE1 2QR"),
        phone = None,
        email = None,
        dxAddress = None,
        reference = Some(getNextAgentRefNumber),
        isAuthorised = None,
        lMigrated = None,
        createDate = java.sql.Timestamp.from(Instant.now()),
        lastUpdateDate = java.sql.Timestamp.from(Instant.now())
      )
    )

  val insertReturnAgent = (recNumber: Int, returnType: ReturnType, nextId: NextId) =>
    DBIO
      .seq(
        Tables.ReturnAgent ++=
          multipleAgentReturns(recNumber, returnType, nextId)
      )
      .transactionally

  // LAND
  val insertMultiLand = (recNumber: Int, returnType: ReturnType, nextId: NextId) =>
    (1 to recNumber).map(id =>
      LandRow(
        landId = BigDecimal(nextId.nextLandId + id),
        returnId = BigDecimal(nextId.nextReturnId + id),
        propertyType = None,
        interestTransferredCreated = None,
        houseNumber = getNextFullAddress.map(_.houseNumber),
        address1 = getNextFullAddress.map(i => i.streetName + " " + i.town),
        address2 = None,
        address3 = None,
        address4 = None,
        postcode = getNextFullAddress.map(_.postCode),
        landArea = None,
        areaUnit = None,
        localAuthorityNumber = None,
        mineralRights = None,
        nlpgUprn = None,
        willSendPlanByPost = None,
        titleNumber = None,
        landResourceRef = None,
        nextLandId = None,
        lMigrated = None,
        createDate = java.sql.Timestamp.from(Instant.now()),
        lastUpdateDate = java.sql.Timestamp.from(Instant.now())
      )
    )

  val insertLand = (recNumber: Int, returnType: ReturnType, nextId: NextId) =>
    DBIO
      .seq(
        Tables.Land ++= insertMultiLand(recNumber, returnType, nextId)
      )
      .transactionally

  // PURCHASER
  val multiplePurchaser = (recNumber: Int, returnType: ReturnType, nextId: NextId) =>
    (1 to recNumber).map(id =>
      PurchaserRow(
        purchaserId = BigDecimal(nextId.nextPurchaserId + id),
        returnId = BigDecimal(nextId.nextReturnId + id),
        isCompany = Some("NO"),
        isTrustee = None,
        isConnectedToVendor = None,
        isRepresentedByAgent = None,
        title = Some("Mr"),
        surname = Some(getNextSureName),
        forename1 = None,
        forename2 = None,
        companyName = Some("companyName 890"),
        houseNumber = Some("houseNumber 131"),
        address1 = Some("Address 17891"),
        address2 = None,
        address3 = None,
        address4 = None,
        postcode = None,
        phone = None,
        nino = None,
        purchaserResourceRef = None,
        nextPurchaserId = None,
        lMigrated = None,
        createDate = java.sql.Timestamp.from(Instant.now()),
        lastUpdateDate = java.sql.Timestamp.from(Instant.now()),
        hasNino = None,
        dateOfBirth = None,
        isUkCompany = None,
        registrationNumber = None,
        placeOfRegistration = None
      )
    )

  val insertPurchaser: (Int, ReturnType, NextId) => DBIOAction[Unit, NoStream, Effect.Write & Effect.Transactional] = (recNumber: Int, returnType: ReturnType, nextId: NextId) =>
    DBIO
      .seq(
        Tables.Purchaser ++= multiplePurchaser(recNumber, returnType, nextId)
      )
      .transactionally

  // SUBMITTION
  val insertMultiSubmittion = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    (1 to recNumber).map(id =>
      SubmissionRow(
        submissionId = BigDecimal(nextId.nextSubmissionId + id),
        returnId = BigDecimal(nextId.nextReturnId + id),
        storn = storn,
        submissionStatus = None,
        govtalkMessageClass = None,
        utrn = Some(getNextUtrn),
        irmarkReceived = None,
        submissionReceipt = None,
        govtalkErrorCode = None,
        govtalkErrorType = None,
        govtalkErrorMessage = None,
        numPolls = None,
        acceptedDate = None,
        submittedDate = None,
        email = None,
        lMigrated = None,
        submissionRequestDate = None,
        createDate = java.sql.Timestamp.from(Instant.now()),
        lastUpdateDate = java.sql.Timestamp.from(Instant.now()),
        irMarkSent = None
      )
    )

  val insertSubmittion = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    DBIO
      .seq(
        Tables.Submission ++= insertMultiSubmittion(recNumber, storn, returnType, nextId)
      )
      .transactionally

}
