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
import pekko.actor.{Actor, Props}
import pekko.event.Logging
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import org.apache.pekko.pattern.pipe
import uk.gov.hmrc.stampdutylandtaxstub.services.OracleDataService

object DataAccessActor {

  case class CreateData(storn: String, numberOfRecords: Option[Int])

  case object DeleteAllData

  case object OperationStatus
  case class OperationStart(cmd: String, arg: String = "")
  case class OperationComplete(failed: Boolean)

  def props = Props[DataAccessActor]()
}

// https://pekko.apache.org/docs/pekko/1.3/actors.html#creating-actors
// https://www.playframework.com/documentation/3.0.x/ScalaPekko

class DataAccessActor extends Actor {

  private val log = Logging(context.system, this)

  // Internal Actor state
  private var locked: Boolean = false
  private var lastError: String = ""
  private var progress: Int = 0

  private val oracleDataService = new OracleDataService()

  def receive: Receive = {
    case CreateData(storn, id) =>
      log.info(s"Status: $locked")
      if (!locked) {
        locked = true
        lastError = ""
        progress = 0
        log.info(s"Ready to start data creation: $id")
        self ! OperationStart("CREATE_DATA", arg = storn)
        sender() ! s"START"
      } else {
        log.info(s"Other operation in progress -> failed to start: $id")
        sender() ! s"BUSY"
      }

    case OperationStart(cmd, arg) =>
      cmd match {
        case "CREATE_DATA" =>
          log.info(s"Start data creation")
          oracleDataService
          .createData(arg)
          .mapTo[OperationComplete].pipeTo(self)
          
        case "DELETE_ALL_DATA" =>
          log.info(s"Start data deletion: $cmd")
          oracleDataService
            .deleteAllData()
            .mapTo[OperationComplete].pipeTo(self)
          
        case _ =>
          log.info(s"UNKNOWN COMMAND: $cmd")
      }


    case OperationComplete(status) =>
      if (!status) {
        log.info(s"DataCreation:Success")
        locked = false
      } else {
        log.info(s"DataCreation:Failed")
        locked = false
      }

    case OperationStatus =>
      log.info(s"Retrieve:Status")
      if (locked) {
        sender() ! s"IN_PROGRESS: ${progress}%"
      } else {
        if (lastError.nonEmpty) {
          sender() ! s"FREE:$lastError"
        } else {
          sender() ! s"FREE"
        }
      }

    case DeleteAllData =>
      log.info(s"Status: $locked")
      if (!locked) {
        locked = true
        lastError = ""
        progress = 0
        log.info(s"Ready to start data deletion")
        self ! OperationStart("DELETE_ALL_DATA")
        sender() ! s"START"
      } else {
        log.info(s"Other operation in progress -> failed to start")
        sender() ! s"BUSY"
      }

    case msg =>
      log.info(s"Unknown message: $msg")
  }

}