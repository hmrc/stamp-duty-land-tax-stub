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
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import org.apache.pekko.pattern.pipe

object DataAccessActor {

  case class CreateData(id: String = UUID.randomUUID().toString)

  case class CreateDataStart(id: String)

  case class DataCreationStatus(id: String)

  case class DataCreationComplete(failed: Boolean)

  def props = Props[DataAccessActor]()
}

// https://pekko.apache.org/docs/pekko/1.3/actors.html#creating-actors
// https://www.playframework.com/documentation/3.0.x/ScalaPekko

class DataAccessActor extends Actor {

  private var locked: Boolean = false
  private var lastError: String = ""
  private var progress: Int = 0
  private val log = Logging(context.system, this)

  def receive: Receive = {
    case CreateData(id) =>
      log.info(s"Status: $locked")
      if (!locked) {
        locked = true
        lastError = ""
        progress = 0
        log.info(s"Ready to start data creation: $id")
        self ! CreateDataStart(id)
        sender() ! s"START"
      } else {
        log.info(s"Other operation in progress -> failed to start: $id")
        sender() ! s"BUSY"
      }

    case CreateDataStart(id) =>
      log.info(s"Start data creation: $id")
      longRunningOps().mapTo[DataCreationComplete].pipeTo(self)

    case DataCreationComplete(status) =>
      if (!status) {
        log.info(s"DataCreation:Success")
        locked = false
      } else {
        log.info(s"DataCreation:Failed")
        locked = false
      }

    case DataCreationStatus(id) =>
      log.info(s"Retrieve:Status: $id")
      if (locked) {
        sender() ! s"IN_PROGRESS: ${progress}%"
      } else {
        if (lastError.nonEmpty) {
          sender() ! s"FREE:$lastError"
        } else {
          sender() ! s"FREE"
        }
      }

    case msg =>
      log.info(s"Unknown message: $msg")
  }

  // Simulating long-running process:: ie ingesting data into OracleDb
  private def longRunningOps(): Future[DataCreationComplete] = {
    Future {
      (0 to 99).foreach(_ => {
        progress += 1
        Thread.sleep(1000)
        log.info(s"Next iteration: $progress")
      })
      DataCreationComplete(false)
    }

  }

}