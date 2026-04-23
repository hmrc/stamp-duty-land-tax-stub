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
import scala.concurrent.{ExecutionContext, Future}
import org.apache.pekko.actor.*
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import play.api.Logging
import play.api.mvc.*
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor
import uk.gov.hmrc.stampdutylandtaxstub.actors.DataAccessActor.{CreateData, DeleteAllData, OperationStatus}
import uk.gov.hmrc.stampdutylandtaxstub.sql.ReturnType.toReturnType
import scala.concurrent.duration.DurationInt

/* Local host testing:
  CREATE_DATA => http://localhost:10914/stamp-duty-land-tax-stub/createData
  GET_STATUS  => http://localhost:10914/stamp-duty-land-tax-stub/getStatus

  Play + Pekko actor integration
  https://www.playframework.com/documentation/3.0.x/ScalaPekko
 */
@Singleton
class OracleAccessController @Inject()(system: ActorSystem,
                                       cc: ControllerComponents)
                                      (implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  private val oracleDataAccessActor = system.actorOf(DataAccessActor.props, "DataAccess-Actor")

  // Timeout for the Ask patter / sync calls to actor
  private implicit val timeout: Timeout = 15.seconds

  def createData(storn: String, numberOfRecords: Option[Int], returnTypeStr: String): Action[AnyContent] = Action.async {
    logger.info(s"[OracleAccessController][createData]: ${storn}-${numberOfRecords}-${returnTypeStr}")
    if (numberOfRecords.getOrElse(0) < 0 || numberOfRecords.getOrElse(0) > 500 ) {
          logger.error(s"[OracleAccessController][createData]: recNumber::${numberOfRecords}")
          Future.successful( Ok("ERROR: permitted number of records between 1 and 500") )
    } else {

      val returnType = toReturnType(returnTypeStr)
      (oracleDataAccessActor ? CreateData(storn, numberOfRecords, returnType))
        .mapTo[String]
        .recoverWith {
          case ex =>
            logger.error(s"[OracleAccessController][createData]: $ex")
            Future.successful(ex.toString)
        }.map { msg =>
          Ok(msg)
        }
    }
  }

  def deleteAll(): Action[AnyContent] = Action.async {
    logger.info(s"[OracleAccessController][deleteAll]")
    (oracleDataAccessActor ? DeleteAllData)
      .recoverWith {
        case ex =>
          logger.error(s"[OracleAccessController][deleteAll]: $ex")
          Future.successful(ex.toString)
      }
      .mapTo[String].map { msg =>
        Ok(msg)
      }
  }

  def getStatus: Action[AnyContent] = Action.async {
    logger.info(s"[OracleAccessController][getStatus]")
    (oracleDataAccessActor ? OperationStatus).mapTo[String]
      .recoverWith {
        case ex =>
          logger.error(s"[OracleAccessController][getStatus]: $ex")
          Future.successful(ex.toString)
      }
      .map { message =>
        Ok(message)
      }
  }

}