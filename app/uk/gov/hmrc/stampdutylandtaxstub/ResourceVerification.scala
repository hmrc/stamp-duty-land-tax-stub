/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.stampdutylandtaxstub

import java.io.InputStream
import scala.io.Source
import scala.util.Try

object ResourceVerification  {

  import quoted._

  // Step 1:: -> quoting 'exp => AST
  inline def resourceSchemaValidationMacros(path: String): String = {
    ${ resourceSchemaValidationMacrosImp('path) }
  }

  def readFile(filePath: String): Option[String] = {
    Try {
      scala.io.Source.fromFile(filePath).getLines().toList.mkString("")
    }.toOption
  }

  def resourceSchemaValidationMacrosImp(pathExpr: Expr[String])(using Quotes): Expr[String] = {
    val path: String = pathExpr.valueOrAbort
    val fileReadRes : Option[String] = readFile(path)
    if (fileReadRes.nonEmpty){
      val res = Expr(s"File name: ${pathExpr}")
      println(s"File content: ${fileReadRes}")
      // Now apply schema validation
      res
    } else {
      throw new Error(s"File simply not found: $path")
    }
  }
}