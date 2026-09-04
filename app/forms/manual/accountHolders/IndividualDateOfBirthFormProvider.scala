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

package forms.manual.accountHolders

import forms.mappings.Mappings
import models.manual.accountHolders.IndividualDateOfBirth
import play.api.data.format.Formatter
import play.api.data.Forms.of
import play.api.data.{Form, FormError}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import scala.util.Try

class IndividualDateOfBirthFormProvider @Inject() extends Mappings {

  private val earliestDate: LocalDate =
    LocalDate.of(1900, 1, 1)

  private val displayDateFormatter: DateTimeFormatter =
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

  def apply(): Form[IndividualDateOfBirth] =
    Form(
      "value" -> of(dateOfBirthFormatter)
    )

  private val dateOfBirthFormatter: Formatter[IndividualDateOfBirth] =
    new Formatter[IndividualDateOfBirth] {

      override def bind(
        key: String,
        data: Map[String, String]
      ): Either[Seq[FormError], IndividualDateOfBirth] = {

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
              IndividualDateOfBirth(
                LocalDate.of(
                  year.toInt,
                  parseMonth(month).get,
                  day.toInt
                )
              )
            )
          }
      }

      override def unbind(
        key: String,
        value: IndividualDateOfBirth
      ): Map[String, String] =
        Map(
          s"$key.day"   -> value.dateOfBirth.getDayOfMonth.toString,
          s"$key.month" -> value.dateOfBirth.getMonthValue.toString,
          s"$key.year"  -> value.dateOfBirth.getYear.toString
        )
    }

  private def validateEmpty(
    key: String,
    day: String,
    month: String,
    year: String
  ): Option[Either[Seq[FormError], IndividualDateOfBirth]] =
    Option.when(
      day.isEmpty &&
        month.isEmpty &&
        year.isEmpty
    ) {
      error(
        key,
        "individualDateOfBirth.error.required"
      )
    }

  private def validateCharacters(
    key: String,
    day: String,
    month: String,
    year: String
  ): Option[Either[Seq[FormError], IndividualDateOfBirth]] = {

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
            "individualDateOfBirth.error.invalidCharacters"
          )
        )

      case _ =>
        Some(
          error(
            key,
            "individualDateOfBirth.error.invalidCharacters"
          )
        )
    }
  }

  private def validateIncompleteDate(
    key: String,
    day: String,
    month: String,
    year: String
  ): Option[Either[Seq[FormError], IndividualDateOfBirth]] = {

    val dayMissing   = day.isEmpty
    val monthMissing = month.isEmpty
    val yearMissing  = year.isEmpty

    (dayMissing, monthMissing, yearMissing) match {
      case (true, false, false) =>
        Some(
          error(
            s"$key.day",
            "individualDateOfBirth.error.day.required"
          )
        )

      case (false, true, false) =>
        Some(
          error(
            s"$key.month",
            "individualDateOfBirth.error.month.required"
          )
        )

      case (false, false, true) =>
        Some(
          error(
            s"$key.year",
            "individualDateOfBirth.error.year.required"
          )
        )

      case (true, true, false) =>
        Some(
          error(
            s"$key.day",
            "individualDateOfBirth.error.dayMonth.required"
          )
        )

      case (true, false, true) =>
        Some(
          error(
            s"$key.day",
            "individualDateOfBirth.error.dayYear.required"
          )
        )

      case (false, true, true) =>
        Some(
          error(
            s"$key.month",
            "individualDateOfBirth.error.monthYear.required"
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
  ): Option[Either[Seq[FormError], IndividualDateOfBirth]] = {

    val parsedDay   = day.toIntOption
    val parsedMonth = parseMonth(month)
    val parsedYear  = year.toIntOption

    val invalidComponents = Seq(
      Option.when(
        parsedDay.forall(
          value => value < 1 || value > 31
        )
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
            "individualDateOfBirth.error.real"
          )
        )

      case fields if fields.nonEmpty =>
        Some(
          error(
            key,
            "individualDateOfBirth.error.real"
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
  ): Option[Either[Seq[FormError], IndividualDateOfBirth]] = {

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
            "individualDateOfBirth.error.real"
          )
        )

      case Some(date) if date.isBefore(earliestDate) =>
        Some(
          error(
            key,
            "individualDateOfBirth.error.past"
          )
        )

      case Some(date) =>
        val today = LocalDate.now()

        if date.isAfter(today) then
          Some(
            error(
              key,
              "individualDateOfBirth.error.future",
              Seq(today.format(displayDateFormatter))
            )
          )
        else None
    }
  }

  private def parseMonth(value: String): Option[Int] = {

    val normalisedMonth =
      value.toLowerCase(Locale.UK)

    if normalisedMonth.forall(_.isDigit) then
      normalisedMonth.toIntOption
        .filter(
          month => month >= 1 && month <= 12
        )
    else monthNames.get(normalisedMonth)
  }

  private def validMonthCharacters(value: String): Boolean =
    value.forall(_.isDigit) || value.forall(_.isLetter)

  private def normalise(value: String): String =
    value.replaceAll("""[\s-]""", "")

  private def error(
    key: String,
    message: String,
    args: Seq[Any] = Seq.empty
  ): Either[Seq[FormError], IndividualDateOfBirth] =
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
