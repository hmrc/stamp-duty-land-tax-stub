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

import play.api.Logging
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.OperationComplete
import uk.gov.hmrc.stampdutylandtaxstub.services.OracleDataService.CreateDataBatchParams
import uk.gov.hmrc.stampdutylandtaxstub.sql.*
import uk.gov.hmrc.stampdutylandtaxstub.sql.DeleteQueries.{deleteAllLandAction, deleteAllOrgsAction, deleteAllPurchaserAction, deleteAllReturnAction, deleteAllReturnAgentAction, deleteAllSubmittedAction, updateReturnMainLandIdAction, updateReturnPurchaserIdAction}
import uk.gov.hmrc.stampdutylandtaxstub.sql.InsertQueries.*
import uk.gov.hmrc.stampdutylandtaxstub.sql.UpdateQueries.{updateReturnMainLandId, updateReturnsMainPurchaserId}

import scala.concurrent.{ExecutionContext, Future}

object OracleDataService {
  case class CreateDataBatchParams(storn: String, returnType: ReturnType, batchSizeMaybe: Option[Int]) {
    val batchSize: Int = batchSizeMaybe.getOrElse(10) // default batch size
  }
}

class OracleDataService extends OracleConnectBase with Logging {

  private val insertAllAction = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    insertReturnAction(recNumber, storn, returnType, nextId) andThen
      insertReturnAgent(recNumber, returnType, nextId) andThen
      insertLand(recNumber, returnType, nextId) andThen
      insertPurchaser(recNumber, returnType, nextId)

  private val insertAllSubmissionAction = (recNumber: Int, storn: String, returnType: ReturnType, nextId: NextId) =>
    insertReturnAction(recNumber, storn, returnType, nextId) andThen
      insertReturnAgent(recNumber, returnType, nextId) andThen
      insertLand(recNumber, returnType, nextId) andThen
      insertPurchaser(recNumber, returnType, nextId) andThen
      insertSubmittion(recNumber, storn, returnType, nextId)

  private def extractNextId(implicit ec: ExecutionContext): Future[NextId] = {
    for {
      maxReturnId <- db.run(maxReturnIdQuery) // Extract Id's
      maxReturnAgentId <- db.run(maxReturnAgentIdQuery)
      maxLandId <- db.run(maxLandIdQuery)
      maxPurchaserId <- db.run(maxPurchaserIdQuery)
      maxSubmissionId <- db.run(maxSubmissionIdQuery)
    } yield
      NextId(
        nextReturnId = maxReturnId.map(_.toInt).getOrElse(1),
        nextReturnAgentId = maxReturnAgentId.map(_.toInt).getOrElse(1),
        nextLandId = maxLandId.map(_.toInt).getOrElse(1),
        nextPurchaserId = maxPurchaserId.map(_.toInt).getOrElse(1),
        nextSubmissionId = maxSubmissionId.map(_.toInt).getOrElse(1)
      )
  }

  def createDataBatch(param: CreateDataBatchParams)
                     (implicit ec: ExecutionContext): Future[OperationComplete] = {
    for {
      orgInsertResult <- db.run(insertOrgAction(param.storn).asTry)
      nextId <- extractNextId
      insertResult <- db.run(param.returnType match {
        case SubmissionReturns | DueForDeletionReturns =>
          insertAllSubmissionAction(param.batchSize, param.storn, param.returnType, nextId).asTry
        case _ =>
          insertAllAction(param.batchSize, param.storn, param.returnType, nextId).asTry
      })
      updateMainLandIdResult <- db.run(updateReturnMainLandId(nextId = nextId, batchSize = param.batchSize).asTry)
      updatePurchaserIdResult <- db.run(updateReturnsMainPurchaserId(nextId = nextId, batchSize = param.batchSize).asTry)
    } yield {
        val errors = Seq(orgInsertResult, insertResult, updateMainLandIdResult, updatePurchaserIdResult)
          .map(_.toEither).collect {
            case Left(error) => error
          }
        if (errors.nonEmpty){
          errors
            .foreach(error =>
              logger.error(s"[OracleDataService][createDataBatch]: error => ${error}")
            )
          OperationComplete(errors.headOption) // return first found error only
        } else {
          OperationComplete(None)
        }
    }
  }

  // TODO: add delete by storn???
  def deleteAllData(implicit ec: ExecutionContext): Future[OperationComplete] = {
    for {
      updateReturnMainLandIdResult <- db.run(updateReturnMainLandIdAction.asTry)
      updateReturnPurchaserIdResult <- db.run(updateReturnPurchaserIdAction.asTry)
      deleteAllPurchaserResult <- db.run(deleteAllPurchaserAction.asTry)
      deleteAllSubmittedResult <- db.run(deleteAllSubmittedAction.asTry)
      deleteAllLandResult <- db.run(deleteAllLandAction.asTry)
      deleteAllReturnAgentResult <- db.run(deleteAllReturnAgentAction.asTry)
      deleteAllReturnResult <- db.run(deleteAllReturnAction.asTry)
      deleteAllOrgsResult <- db.run(deleteAllOrgsAction.asTry)
    } yield {
      val errors = Seq(updateReturnMainLandIdResult, updateReturnPurchaserIdResult,
        deleteAllPurchaserResult, deleteAllSubmittedResult, deleteAllLandResult,
        deleteAllReturnAgentResult, deleteAllReturnResult, deleteAllOrgsResult)
        .map(_.toEither)
        .collect {
          case Left(error) => error
        }
      errors
        .foreach(error =>
          logger.error(s"[OracleDataService][deleteAllData]: error => ${error}")
        )
      if (errors.nonEmpty) {
        OperationComplete(errors.headOption)
      } else {
        OperationComplete(None)
      }

    }
  }

}