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

package uk.gov.hmrc.stampdutylandtaxstub.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import org.apache.pekko.actor.*
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import play.api.mvc.*
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.{CreateData, DataCreationStatus}

import scala.concurrent.duration.DurationInt

/* Local host testing:
  CREATE_DATA => http://localhost:10914/stamp-duty-land-tax-stub/createData
  GET_STATUS  => http://localhost:10914/stamp-duty-land-tax-stub/getStatus
 */

@Singleton
class OracleAccessController @Inject()(system: ActorSystem,
                                       cc: ControllerComponents)
                                        (implicit ec: ExecutionContext)
  extends BackendController(cc) {

// https://www.playframework.com/documentation/3.0.x/ScalaPekko


  private val dataAccessActor = system.actorOf(DataAccessActor.props, "DataAccess-Actor")

  private implicit val timeout: Timeout = 5.seconds

  def startOperation: Action[AnyContent] = Action.async {
    (dataAccessActor ? CreateData() ).mapTo[String].map {
      message =>
        Ok(message)
    }
  }

  def getOpsStatus: Action[AnyContent] = Action.async {
    (dataAccessActor ? DataCreationStatus("")).mapTo[String].map {
      message =>
        Ok(message)
    }
  }

}