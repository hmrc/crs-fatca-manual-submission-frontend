/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.mappings

trait Transforms {

  protected def stripSpaces(string: String): String =
    string.trim.replaceAll(" ", "")

  protected def validPostCodeFormat(validPostCode: String): String =
    if (!validPostCode.contains(" ")) {
      val tail = validPostCode.substring(validPostCode.length - 3)
      val head = validPostCode.substring(0, validPostCode.length - 3)
      s"$head $tail".toUpperCase
    } else { validPostCode.toUpperCase }

  protected def minimiseSpace(value: String): String =
    value.replaceAll(" {2,}", " ")

  private[mappings] def postCodeTransform(value: String): String =
    minimiseSpace(value.trim.toUpperCase)

  protected def postCodeDataTransform(value: Option[String]): Option[String] =
    value.map(postCodeTransform).filter(_.nonEmpty)

  protected def formatAmount(value: String): String = {
    val strippedValue = value.replaceAll("[\\s,]+", "")

    if (strippedValue.contains('.')) {
      formatDecimal(tidyLeadingDecimal(strippedValue))
    } else {
      formatInteger(strippedValue)
    }
  }

  private def tidyLeadingDecimal(numStr: String): String = numStr match {
    case x if x.startsWith(".")  => "0" + x
    case x if x.startsWith("-.") => "-0" + x.substring(1)
    case x                       => x
  }

  private def removeLeadingZeros(numStr: String): String =
    if (numStr.startsWith("-")) {
      val abs = numStr.substring(1).replaceAll("^0+", "")
      if (abs.isEmpty) "-0" else "-" + abs
    } else {
      val abs = numStr.replaceAll("^0+", "")
      if (abs.isEmpty) "0" else abs
    }

  private def formatInteger(numStr: String): String =
    formatLargeNumber(removeLeadingZeros(numStr))

  private def formatLargeNumber(numStr: String): String = {
    val (sign, rest)  = if (numStr.startsWith("-")) ("-", numStr.substring(1)) else ("", numStr)
    val formattedRest = rest.reverse.grouped(3).mkString(",").reverse
    s"$sign$formattedRest"
  }

  private def formatDecimal(numStr: String): String = {
    val parts             = numStr.split("\\.", 2)
    val intPart           = formatLargeNumber(removeLeadingZeros(parts(0)))
    val decimalPart       = if (parts.length > 1) parts(1) else ""
    val paddedDecimalPart = decimalPart.take(2).padTo(2, '0')
    formatWholeDecimal(intPart, paddedDecimalPart)
  }

  private def formatWholeDecimal(intPart: String, deciPart: String): String =
    if (deciPart == "00") {
      intPart match {
        case "-0"  => "0"
        case other => other
      }
    } else {
      s"$intPart.$deciPart"
    }
}
