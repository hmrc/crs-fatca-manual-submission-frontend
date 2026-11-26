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

package services.json

import com.jayway.jsonpath.JsonPath
import play.api.libs.json.*

object JsonPathExtractor:

  def read(
            json: JsValue,
            path: String
          ): Option[String] =
    try {

      Option(
        JsonPath.read[Any](
          Json.stringify(json),
          path
        )
      ).map(_.toString)

    } catch {
      case _: Throwable => None
    }

  def readArray(
                 json: JsValue,
                 path: String
               ): Seq[JsValue] =

    try {

      val fieldName =
        path
          .stripPrefix("$.")
          .stripSuffix("[*]")

      (json \ fieldName)
        .asOpt[JsArray]
        .map(_.value.toSeq)
        .getOrElse(Seq.empty)

    } catch {
      case _: Throwable => Seq.empty
    }