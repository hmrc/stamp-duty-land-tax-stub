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
import uk.gov.hmrc.stampdutylandtaxstub.sql.OracleConnect.{insertRecords, postInsertUpdate}
import uk.gov.hmrc.stampdutylandtaxstub.sql.{DueForDeletionReturns, OracleConnectBase}

import scala.concurrent.{ExecutionContext, Future}

class OracleDataService extends OracleConnectBase {

  // Simulate Creation and DataAll deletion

  def createData(storn: String)
                (implicit ec: ExecutionContext): Future[Unit] = Future {
      val recNumber : Int = 100
      for {
        _ <- Future {
          insertRecords(100, storn, DueForDeletionReturns)
        }
        _ <- Future {
          postInsertUpdate(recNumber, DueForDeletionReturns)
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