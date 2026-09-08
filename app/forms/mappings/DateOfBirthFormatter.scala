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

package forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.util.Try

class DateOfBirthFormatter(
                            requiredKey: String,
                            invalidCharactersKey: String,
                            dayRequiredKey: String,
                            monthRequiredKey: String,
                            yearRequiredKey: String,
                            dayMonthRequiredKey: String,
                            dayYearRequiredKey: String,
                            monthYearRequiredKey: String,
                            realDateKey: String,
                            pastKey: String,
                            futureKey: String
                          ) extends Formatter[LocalDate] {

  private val earliestDate =
    LocalDate.of(1900, 1, 1)

  private val displayDateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

  private val monthNames: Map[String, Int] = Map(
    "jan"       -> 1,
    "january"   -> 1,
    "feb"       -> 2,
    "february"  -> 2,
    "mar"       -> 3,
    "march"     -> 3,
    "apr"       -> 4,
    "april"     -> 4,
    "may"       -> 5,
    "jun"       -> 6,
    "june"      -> 6,
    "jul"       -> 7,
    "july"      -> 7,
    "aug"       -> 8,
    "august"    -> 8,
    "sep"       -> 9,
    "september" -> 9,
    "oct"       -> 10,
    "october"   -> 10,
    "nov"       -> 11,
    "november"  -> 11,
    "dec"       -> 12,
    "december"  -> 12
  )

  override def bind(
                     key: String,
                     data: Map[String, String]
                   ): Either[Seq[FormError], LocalDate] = {

    val day =
      normalise(data.getOrElse(s"$key.day", ""))

    val month =
      normalise(data.getOrElse(s"$key.month", ""))

    val year =
      normalise(data.getOrElse(s"$key.year", ""))

    validateEmpty(key, day, month, year)
      .orElse(validateCharacters(key, day, month, year))
      .orElse(validateIncompleteDate(key, day, month, year))
      .orElse(validateDate(key, day, month, year))
      .getOrElse {
        Right(
          LocalDate.of(
            year.toInt,
            parseMonth(month).get,
            day.toInt
          )
        )
      }
  }

  override def unbind(
                       key: String,
                       value: LocalDate
                     ): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )

  private def validateEmpty(
                             key: String,
                             day: String,
                             month: String,
                             year: String
                           ): Option[Either[Seq[FormError], LocalDate]] =
    Option.when(
      day.isEmpty &&
        month.isEmpty &&
        year.isEmpty
    ) {
      error(
        key,
        requiredKey
      )
    }

  private def validateCharacters(
                                  key: String,
                                  day: String,
                                  month: String,
                                  year: String
                                ): Option[Either[Seq[FormError], LocalDate]] = {

    val invalidFields = Seq(
      Option.when(day.nonEmpty && !day.forall(_.isDigit))("day"),
      Option.when(month.nonEmpty && !validMonthCharacters(month))("month"),
      Option.when(year.nonEmpty && !year.forall(_.isDigit))("year")
    ).flatten

    invalidFields match {
      case Seq() =>
        None

      case Seq(field) =>
        Some(
          error(
            s"$key.$field",
            invalidCharactersKey
          )
        )

      case _ =>
        Some(
          error(
            key,
            invalidCharactersKey
          )
        )
    }
  }

  private def validateIncompleteDate(
                                      key: String,
                                      day: String,
                                      month: String,
                                      year: String
                                    ): Option[Either[Seq[FormError], LocalDate]] = {

    val dayMissing   = day.isEmpty
    val monthMissing = month.isEmpty
    val yearMissing  = year.isEmpty

    (dayMissing, monthMissing, yearMissing) match {

      case (true, false, false) =>
        Some(
          error(
            s"$key.day",
            dayRequiredKey
          )
        )

      case (false, true, false) =>
        Some(
          error(
            s"$key.month",
            monthRequiredKey
          )
        )

      case (false, false, true) =>
        Some(
          error(
            s"$key.year",
            yearRequiredKey
          )
        )

      case (true, true, false) =>
        Some(
          error(
            s"$key.day",
            dayMonthRequiredKey
          )
        )

      case (true, false, true) =>
        Some(
          error(
            s"$key.day",
            dayYearRequiredKey
          )
        )

      case (false, true, true) =>
        Some(
          error(
            s"$key.month",
            monthYearRequiredKey
          )
        )

      case _ =>
        None
    }
  }

  private def validateDate(
                            key: String,
                            day: String,
                            month: String,
                            year: String
                          ): Option[Either[Seq[FormError], LocalDate]] = {

    val parsedDay   = day.toIntOption
    val parsedMonth = parseMonth(month)
    val parsedYear  = year.toIntOption

    val invalidComponents = Seq(
      Option.when(
        parsedDay.forall(value => value < 1 || value > 31)
      )("day"),
      Option.when(
        parsedMonth.isEmpty
      )("month"),
      Option.when(
        parsedYear.forall(_ < 1)
      )("year")
    ).flatten

    invalidComponents match {

      case Seq(field) =>
        Some(
          error(
            s"$key.$field",
            realDateKey
          )
        )

      case fields if fields.nonEmpty =>
        Some(
          error(
            key,
            realDateKey
          )
        )

      case _ =>
        validateRealDate(
          key,
          parsedDay.get,
          parsedMonth.get,
          parsedYear.get
        )
    }
  }

  private def validateRealDate(
                                key: String,
                                day: Int,
                                month: Int,
                                year: Int
                              ): Option[Either[Seq[FormError], LocalDate]] = {

    val maybeDate =
      Try(
        LocalDate.of(
          year,
          month,
          day
        )
      ).toOption

    maybeDate match {

      case None =>
        Some(
          error(
            key,
            realDateKey
          )
        )

      case Some(date) if date.isBefore(earliestDate) =>
        Some(
          error(
            key,
            pastKey
          )
        )

      case Some(date) =>
        val today = LocalDate.now()

        if date.isAfter(today) then
          Some(
            error(
              key,
              futureKey,
              Seq(today.format(displayDateFormatter))
            )
          )
        else
          None
    }
  }

  private def parseMonth(value: String): Option[Int] = {

    val normalisedMonth =
      value.toLowerCase(Locale.UK)

    if normalisedMonth.forall(_.isDigit) then
      normalisedMonth
        .toIntOption
        .filter(month => month >= 1 && month <= 12)
    else
      monthNames.get(normalisedMonth)
  }

  private def validMonthCharacters(value: String): Boolean =
    value.forall(_.isDigit) || value.forall(_.isLetter)

  private def normalise(value: String): String =
    value.replaceAll("""[\s-]""", "")

  private def error(
                     key: String,
                     message: String,
                     args: Seq[Any] = Seq.empty
                   ): Either[Seq[FormError], LocalDate] =
    Left(
      Seq(
        FormError(
          key,
          message,
          args
        )
      )
    )
}