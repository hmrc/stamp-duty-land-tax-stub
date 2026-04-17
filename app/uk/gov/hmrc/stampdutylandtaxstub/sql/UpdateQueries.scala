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

import uk.gov.hmrc.stampdutylandtaxstub.sql.InsertQueries.NextId
import uk.gov.hmrc.stampdutylandtaxstub.sql.Tables.*
import uk.gov.hmrc.stampdutylandtaxstub.sql.Tables.profile.api.*

import scala.concurrent.Future
import scala.language.postfixOps


// TODO: for some reason we still need this Thread.sleep to prevent error
// > java.sql.SQLException: Listener refused the connection with the following error
// Too many DB connections ??
object UpdateQueries {

  def updateReturnMainLandId(id: Int, nextId: NextId)(implicit
                                                                              db: profile.backend.JdbcDatabaseDef
  ): Future[_] = {
    Thread.sleep(100)
    db.run(
      Tables.Return
        .filter(_.returnId === BigDecimal(nextId.nextReturnId + id))
        .map(_.mainLandId)
        .update(Some(BigDecimal(nextId.nextLandId + id)))
        .transactionally
    )
  }

  def updateReturnsMainPurchaserId(id: Int, nextId: NextId)(implicit
    db: profile.backend.JdbcDatabaseDef
  ): Future[Int] = {
    Thread.sleep(100)
    val action = Tables.Return
      .filter(_.returnId === BigDecimal(nextId.nextReturnId + id))
      .map(_.mainPurchaserId)
      .update(Some(BigDecimal(nextId.nextPurchaserId + id)))
      .transactionally
    db.run(action)
  }

}
