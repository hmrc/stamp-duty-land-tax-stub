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

import models.response.SdltReturnRecordResponse
import org.apache.pekko.http.scaladsl.model.Uri.Path
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.nio.file.{FileSystems, Files}
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

  import scala.jdk.CollectionConverters._

  // TODO: instead of passing concrete file path we need to pass folder path
  // and check each file in the folder for schema correctness
  def resourceSchemaValidationMacrosImp(pathExpr: Expr[String])(using Quotes): Expr[String] = {
    val path: String = pathExpr.valueOrAbort
    val dir = FileSystems.getDefault.getPath(path)
    val files = Files.walk(dir).iterator().asScala

    files
      .filter(f => f.toFile.isFile)
      .map { file =>
      val fileReadRes: Option[String] = readFile(file.toFile.getPath)
      if (fileReadRes.nonEmpty) {
      fileReadRes match {
        case Some(content) =>
          // TODO: we need to have a check if json valid at all
          Json.parse(content).validate[SdltReturnRecordResponse] match {
            case JsSuccess(value, path) =>
              val res = Expr(s"File name: ${pathExpr}")
              println(s"This json file is valid: ${file.toFile.getPath}")
              res
            case JsError(errors) =>
              throw new Error(s"Invalid json in file: ${file.toFile.getPath} / error: $errors")
          }
        case None =>
          throw new Error(s"File not found: $path")
      }
    } else {
        throw new Error(s"File not found: $path")
      }
    }.toList.head
  }
}