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

import models.response.{AgentDetailsResponse, SdltReturnRecordResponse, SubmitAgentDetailsResponse}
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json, JsonValidationError}

import java.nio.file.{FileSystems, Files}
import scala.util.Try

object ResourceVerification {

  import quoted._

  // Step 1:: -> quoting 'exp => AST
  inline def jsonFilesSchemaValidationMacros(): String = {
    ${ resourceSchemaValidationMacrosImp() }
  }

  def readFile(filePath: String): Option[String] = {
    Try {
      scala.io.Source.fromFile(filePath).getLines().toList.mkString("")
    }.toOption
  }

  import scala.jdk.CollectionConverters._

  def getFilesList(prefix: String): List[String] = {
    val currentPath = FileSystems.getDefault
      .getPath("")
      .toAbsolutePath
      .toString
    val dirPath: String = s"$currentPath/conf/$prefix/"
    val dir = FileSystems.getDefault.getPath(dirPath)
    val files = Files.walk(dir).iterator().asScala.toList
    files
      .filter(f => f.toFile.isFile)
      .map(_.toFile.getPath)
  }

  val readFiles = (folderPath: String) => {
    getFilesList(folderPath)
      .map(fileName => (fileName, readFile(fileName)))
      .filter(p => p._2.nonEmpty)
  }

  def resourceSchemaValidationMacrosImp()(using Quotes): Expr[String] = {
    val folderToVerifyFilesSchema: Seq[String] = Seq(
      "resources.manage.allReturns",
      "legacy.resources.manage.agentDetails")

    val errors = folderToVerifyFilesSchema.flatMap {
      case folderPath if folderPath.endsWith("allReturns") =>
        readFiles(folderPath)
          .collect {
            case (fileName, Some(content)) =>
              Json.parse(content).validate[SdltReturnRecordResponse] match {
                case JsSuccess(value, path) =>
                  None
                case JsError(errors) =>
                    getFormattedError(folderPath, fileName, errors)
              }
            case (fileName, None) =>
              Some(s"File with Empty content found: $fileName")
          }
      case folderPath if folderPath.endsWith("agentDetails") =>
        readFiles(folderPath)
          .collect {
            case (fileName, Some(content)) =>
              Json.parse(content).validate[AgentDetailsResponse] match {
                case JsSuccess(value, path) =>
                  None
                case JsError(errors) =>
                  getFormattedError(folderPath, fileName, errors)
              }
            case (fileName, None) =>
              Some(s"File with Empty content found: $fileName")
          }
      // TODO: extend schema validation for other json folders
      case _ =>
        List.empty
    }.flatten

    // Compile time evaluation: DATA COMPILATION
    val firstErrorFound = errors.find(e => e.nonEmpty)
    if (firstErrorFound.isDefined) {
      throw new Error(firstErrorFound.get)
    } else {
      Expr(s"All files in the provided folder are valid")
    }

  }

  private def getFormattedError(folderPath: String, fileName: String, errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]) = {
    val e = errors
      .toList
      .map(p => s"${p._1.toString} = ${p._2}")
      .mkString(" ")
    Some(
      s"\nFolder: $folderPath \n:: File name: $fileName \n=> $e"
    )
  }
}