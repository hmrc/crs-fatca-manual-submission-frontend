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

package models.viewModels.manual.cpso

import play.api.libs.json.{Format, JsString, Reads, Writes}

import scala.annotation.tailrec
import scala.util.Random

case class CPSOId(value: String)

object CPSOId {

  implicit val format: Format[CPSOId] =
    Format(Reads.StringReads.map(CPSOId.apply),
           Writes(
             id => JsString(id.value)
           )
    )

  private val random = new Random()

  @tailrec
  final def generate(existingIds: Set[String]): CPSOId = {

    val id = CPSOId(random.nextInt(99).toString)

    if (existingIds.contains(id.value)) {
      generate(existingIds)
    } else {
      id
    }
  }
}
