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

package uk.gov.hmrc.stampdutylandtaxstub.services

import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.OperationComplete
import uk.gov.hmrc.stampdutylandtaxstub.sql.DeleteQueries.{deleteAllLandAction, deleteAllOrgsAction, deleteAllPurchaserAction, deleteAllReturnAction, deleteAllReturnAgentAction, deleteAllSubmittedAction, updateReturnMainLandIdAction, updateReturnPurchaserIdAction}
import uk.gov.hmrc.stampdutylandtaxstub.sql.InsertQueries.*
import uk.gov.hmrc.stampdutylandtaxstub.sql.UpdateQueries.{updateReturnMainLandId, updateReturnsMainPurchaserId}
import uk.gov.hmrc.stampdutylandtaxstub.sql.*
import scala.concurrent.{ExecutionContext, Future}

class OracleDataService extends OracleConnectBase {

  val insertAllAction = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    insertReturnAction(recNumber, storn, returnType, nextId) andThen
      insertReturnAgent(recNumber, returnType, nextId) andThen
      insertLand(recNumber, returnType, nextId) andThen insertPurchaser(recNumber, returnType, nextId)

  def createData(storn: String, returnType: ReturnType, recNumberMaybe: Option[Int])
                (implicit ec: ExecutionContext): Future[OperationComplete] = {
    val recNumber: Int = recNumberMaybe.getOrElse(5)
    for {
      _ <- db.run(insertOrgAction(storn)) // Suppress error in case this Org already exists
        .map(_ => "OK")
        .recover(_ => "ERROR")
      maxReturnId <- db.run(maxReturnIdQuery) // Extract Id's
      maxReturnAgentId <- db.run(maxReturnAgentIdQuery)
      maxLandId <- db.run(maxLandIdQuery)
      maxPurchaserId <- db.run(maxPurchaserIdQuery)
      maxSubmissionId <- db.run(maxSubmissionIdQuery)
      nextId <- Future.successful(
        NextId(
          nextReturnId = maxReturnId.map(_.toInt).getOrElse(1),
          nextReturnAgentId = maxReturnAgentId.map(_.toInt).getOrElse(1),
          nextLandId = maxLandId.map(_.toInt).getOrElse(1),
          nextPurchaserId = maxPurchaserId.map(_.toInt).getOrElse(1),
          nextSubmissionId = maxSubmissionId.map(_.toInt).getOrElse(1)
        )
      )
      _ <- db.run(
        insertAllAction(recNumber, storn, returnType, nextId
        )
      )
      _ <- returnType match { // Extra insert calls for submissions
        case SubmissionReturns | DueForDeletionReturns =>
          db.run(insertSubmittion(recNumber, storn, returnType, nextId)).map(_ => "SUBS")
        case _ =>
          Future.successful("NO_SUBS")
      }
      _ <- Future.sequence {
        for {
          id <- 1 to recNumber
        } yield updateReturnMainLandId(id, nextId = nextId)
      }
      _ <- Future.sequence {
        for {
          id <- 1 to recNumber
        } yield updateReturnsMainPurchaserId(id, nextId = nextId)
      }
    } yield OperationComplete(false)
  }

  // Async version for: purgeDbStep method
  def deleteAllData(implicit ec: ExecutionContext): Future[OperationComplete] = {
    for {
      _ <- db.run(updateReturnMainLandIdAction)
      _ <- db.run(updateReturnPurchaserIdAction)
      _ <- db.run(deleteAllPurchaserAction)
      _ <- db.run(deleteAllSubmittedAction)
      _ <- db.run(deleteAllLandAction)
      _ <- db.run(deleteAllReturnAgentAction)
      _ <- db.run(deleteAllReturnAction)
      _ <- db.run(deleteAllOrgsAction)
    } yield OperationComplete(false)
  }


}