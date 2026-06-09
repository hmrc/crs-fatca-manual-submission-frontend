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

package queries

import models.SubmissionsConstants.{CRFA, CRS, FATCA, RegimeType}
import play.api.mvc.QueryStringBindable

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PathBinders:

  given QueryStringBindable[LocalDateTime] with
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override def bind(
      key: String,
      params: Map[String, Seq[String]]
    ): Option[Either[String, LocalDateTime]] =
      params.get(key).flatMap(_.headOption).map {
        raw =>
          try Right(LocalDateTime.parse(raw, formatter))
          catch case _: Exception => Left(s"Invalid LocalDateTime: $raw")
      }

    override def unbind(key: String, value: LocalDateTime): String =
      s"$key=${URLEncoder.encode(value.format(formatter), StandardCharsets.UTF_8)}"

  given QueryStringBindable[RegimeType] with

    override def bind(
      key: String,
      params: Map[String, Seq[String]]
    ): Option[Either[String, RegimeType]] =
      params.get(key).flatMap(_.headOption).map {
        raw =>
          raw.trim.toUpperCase match
            case "CRS"   => Right(CRS)
            case "FATCA" => Right(FATCA)
            case "CRFA"  => Right(CRFA)
            case other   => Left(s"Invalid regime type: $other")
      }

    override def unbind(key: String, value: RegimeType): String =
      s"$key=${URLEncoder.encode(value.value, StandardCharsets.UTF_8)}"
