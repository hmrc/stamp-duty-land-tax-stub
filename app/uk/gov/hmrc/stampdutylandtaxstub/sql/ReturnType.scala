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

sealed trait ReturnType

case object InProgressReturns extends ReturnType

case object SubmissionReturns extends ReturnType

case object DueForDeletionReturns extends ReturnType

object ReturnType {

  def toReturnType(returnType: String): ReturnType = {
    returnType match {
      case "inprogress" =>
        InProgressReturns
      case "submission" =>
        SubmissionReturns
      case "deletion" =>
        DueForDeletionReturns
      case _ =>
        InProgressReturns
    }
  }

  def getReturnIdRangeStart(returnType: ReturnType): Int =
    returnType match {
      case InProgressReturns     =>
        1
      case SubmissionReturns     =>
        1000
      case DueForDeletionReturns =>
        2000
      case _                     =>
        throw new Exception(s"Not supported returnType: $returnType")
    }

  def getReturnAgentIdRangeStart(returnType: ReturnType): Int =
    returnType match {
      case InProgressReturns     =>
        30001
      case SubmissionReturns     =>
        50001
      case DueForDeletionReturns =>
        70001
      case _                     =>
        throw new Exception(s"Not supported returnType: $returnType")
    }

  

}
