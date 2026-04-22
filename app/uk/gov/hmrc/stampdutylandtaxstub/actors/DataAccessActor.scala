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

package uk.gov.hmrc.stampdutylandtaxstub.actors

import org.apache.pekko
import org.apache.pekko.actor.{Actor, Props}
import org.apache.pekko.pattern.pipe
import play.api.Logging
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.*
import uk.gov.hmrc.stampdutylandtaxstub.services.OracleDataService
import uk.gov.hmrc.stampdutylandtaxstub.services.OracleDataService.CreateDataBatchParams
import uk.gov.hmrc.stampdutylandtaxstub.sql.{InProgressReturns, ReturnType}

import scala.concurrent.ExecutionContext

object DataAccessActor {

  case class CreateData(storn: String, numberOfRecords: Option[Int], returnType: ReturnType)

  case object DeleteAllData

  case object OperationStatus

  private case class OperationStart(cmd: String, storn: String = "",
                                    returnType: ReturnType = InProgressReturns, recNum: Option[Int])

  case class OperationComplete(errorMaybe: Option[Throwable])

  def props = Props[DataAccessActor]()
}

// https://pekko.apache.org/docs/pekko/1.3/actors.html#creating-actors
// https://www.playframework.com/documentation/3.0.x/ScalaPekko

class DataAccessActor
    extends Actor with Logging {

  implicit val ec: ExecutionContext = context.dispatcher

  // Internal Actor state
  private var locked: Boolean = false
  private var lastError: String = ""

  private lazy val oracleDataService = new OracleDataService()

  override def preStart(): Unit = {
    logger.info("Starting DataAccessActor ...")
    super.preStart()
  }

  override def postStop(): Unit = {
    logger.info("Stopping DataAccessActor ...")
    super.postStop()
  }

  def receive: Receive = {
    case CreateData(storn, recNumber, returnType) =>
      logger.info(s"Status: $locked")
      if (!locked) {
        locked = true
        lastError = ""
        logger.info(s"Ready to start data creation: $recNumber")
        self ! OperationStart("CREATE_DATA", storn = storn, returnType = returnType, recNumber)
        sender() ! s"START"
      } else {
        logger.info(s"Other operation in progress -> failed to start")
        sender() ! s"BUSY"
      }

    case OperationStart(cmd, storn, returnType, recNumber) =>
      cmd match {
        case "CREATE_DATA" =>
          logger.info(s"Start data creation")
          val param = CreateDataBatchParams(storn = storn, returnType = returnType, batchSizeMaybe = recNumber)
          oracleDataService
            .createDataBatch(param)
            .mapTo[OperationComplete].pipeTo(self)

        case "DELETE_ALL_DATA" =>
          logger.info(s"Start data deletion: $cmd")
          oracleDataService
            .deleteAllData()
            .mapTo[OperationComplete].pipeTo(self)

        case _ =>
          logger.info(s"UNKNOWN COMMAND: $cmd")
      }


    case OperationComplete(Some(error)) =>
      logger.info(s"DataCreation:Failed: ${error}")
      locked = false

    case OperationComplete(None) =>
      logger.info(s"DataCreation:Success")
      locked = false

    case OperationStatus =>
      logger.info(s"Retrieve:Status")
      if (locked) {
        sender() ! s"IN_PROGRESS"
      } else {
        if (lastError.nonEmpty) {
          sender() ! s"FREE:$lastError"
        } else {
          sender() ! s"FREE"
        }
      }

    case DeleteAllData =>
      logger.info(s"Status: $locked")
      if (!locked) {
        locked = true
        lastError = ""
        logger.info(s"Ready to start data deletion")
        self ! OperationStart("DELETE_ALL_DATA", "", returnType = InProgressReturns, recNum = None)
        sender() ! s"START"
      } else {
        logger.info(s"Other operation in progress -> failed to start")
        sender() ! s"BUSY"
      }

    case msg =>
      logger.info(s"Unknown message: $msg")
  }

}