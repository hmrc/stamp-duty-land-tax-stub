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
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.CreateData

object DataAccessActor {

  case class CreateData(id: String)

  def props = Props[DataAccessActor]()
}

// https://pekko.apache.org/docs/pekko/1.3/actors.html#creating-actors
class DataAccessActor extends Actor {
  private var ticksCounter: Int = 0

  val log = Logging(context.system, this)

  def receive = {
    case CreateData(_) =>
      if (ticksCounter < 10) {
        ticksCounter += 1
      } else {
        ticksCounter = 0
      }
      log.info(s"Ticks: ${ticksCounter}")
      sender() ! s"Ticks: ${ticksCounter}"
    case _ =>
      log.info("Unknown message")
  }

}