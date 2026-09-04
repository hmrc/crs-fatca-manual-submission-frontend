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

import models.manual.accountHolders.IndividualDateOfBirth
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class IndividualDateOfBirthFormProviderSpec extends AnyFreeSpec with Matchers {

  private val form =
    new IndividualDateOfBirthFormProvider()()

  private val requiredKey =
    "individualDateOfBirth.error.required"

  private val invalidCharactersKey =
    "individualDateOfBirth.error.invalidCharacters"

  private val dayRequiredKey =
    "individualDateOfBirth.error.day.required"

  private val monthRequiredKey =
    "individualDateOfBirth.error.month.required"

  private val yearRequiredKey =
    "individualDateOfBirth.error.year.required"

  private val dayMonthRequiredKey =
    "individualDateOfBirth.error.dayMonth.required"

  private val dayYearRequiredKey =
    "individualDateOfBirth.error.dayYear.required"

  private val monthYearRequiredKey =
    "individualDateOfBirth.error.monthYear.required"

  private val realDateKey =
    "individualDateOfBirth.error.real"

  private val pastKey =
    "individualDateOfBirth.error.past"

  private val futureKey =
    "individualDateOfBirth.error.future"

  private def dateData(
    day: String,
    month: String,
    year: String
  ): Map[String, String] =
    Map(
      "value.day"   -> day,
      "value.month" -> month,
      "value.year"  -> year
    )

  "IndividualDateOfBirthFormProvider" - {

    "must bind a valid numeric date" in {

      val result =
        form.bind(
          dateData(
            day = "31",
            month = "3",
            year = "1980"
          )
        )

      result.value mustBe Some(
        IndividualDateOfBirth(
          LocalDate.of(1980, 3, 31)
        )
      )

      result.errors mustBe empty
    }

    "must accept a numeric month with a leading zero" in {

      val result =
        form.bind(
          dateData(
            day = "31",
            month = "03",
            year = "1980"
          )
        )

      result.value mustBe Some(
        IndividualDateOfBirth(
          LocalDate.of(1980, 3, 31)
        )
      )
    }

    "must accept full month names regardless of case" in {

      val validMonths = Seq(
        "January",
        "january",
        "jaNuary",
        "JANUARY"
      )

      validMonths.foreach {
        month =>
          withClue(s"month '$month' should be accepted") {

            val result =
              form.bind(
                dateData(
                  day = "1",
                  month = month,
                  year = "2000"
                )
              )

            result.value mustBe Some(
              IndividualDateOfBirth(
                LocalDate.of(2000, 1, 1)
              )
            )

            result.errors mustBe empty
          }
      }
    }

    "must accept the first 3 characters of a month name regardless of case" in {

      val validMonths = Seq(
        "Jan",
        "jan",
        "JAN",
        "jAn"
      )

      validMonths.foreach {
        month =>
          withClue(s"month '$month' should be accepted") {

            val result =
              form.bind(
                dateData(
                  day = "1",
                  month = month,
                  year = "2000"
                )
              )

            result.value mustBe Some(
              IndividualDateOfBirth(
                LocalDate.of(2000, 1, 1)
              )
            )

            result.errors mustBe empty
          }
      }
    }

    "must strip spaces before validating" in {

      val result =
        form.bind(
          dateData(
            day = "3 1",
            month = "0 3",
            year = "19 80"
          )
        )

      result.value mustBe Some(
        IndividualDateOfBirth(
          LocalDate.of(1980, 3, 31)
        )
      )

      result.errors mustBe empty
    }

    "must strip hyphens before validating" in {

      val result =
        form.bind(
          dateData(
            day = "3-1",
            month = "0-3",
            year = "19-80"
          )
        )

      result.value mustBe Some(
        IndividualDateOfBirth(
          LocalDate.of(1980, 3, 31)
        )
      )

      result.errors mustBe empty
    }

    "must strip hyphens from month names before validating" in {

      val result =
        form.bind(
          dateData(
            day = "1",
            month = "Jan-uary",
            year = "2000"
          )
        )

      result.value mustBe Some(
        IndividualDateOfBirth(
          LocalDate.of(2000, 1, 1)
        )
      )

      result.errors mustBe empty
    }

    "Empty" - {

      "must return the required error when all fields are empty" in {

        val result =
          form.bind(
            dateData(
              day = "",
              month = "",
              year = ""
            )
          )

        result.errors must contain only FormError(
          "value",
          requiredKey
        )
      }
    }

    "Invalid characters" - {

      "must return the invalid characters error for an invalid day" in {

        val result =
          form.bind(
            dateData(
              day = "1a",
              month = "3",
              year = "1980"
            )
          )

        result.errors must contain only FormError(
          "value.day",
          invalidCharactersKey
        )
      }

      "must return the invalid characters error for an invalid month" in {

        val result =
          form.bind(
            dateData(
              day = "1",
              month = "J@n",
              year = "1980"
            )
          )

        result.errors must contain only FormError(
          "value.month",
          invalidCharactersKey
        )
      }

      "must return the invalid characters error for an invalid year" in {

        val result =
          form.bind(
            dateData(
              day = "1",
              month = "3",
              year = "19a0"
            )
          )

        result.errors must contain only FormError(
          "value.year",
          invalidCharactersKey
        )
      }

      "must return one parent error when multiple fields contain invalid characters" in {

        val result =
          form.bind(
            dateData(
              day = "1a",
              month = "3",
              year = "19@0"
            )
          )

        result.errors must contain only FormError(
          "value",
          invalidCharactersKey
        )
      }

      "must prioritise invalid characters over an incomplete date" in {

        val result =
          form.bind(
            dateData(
              day = "1a",
              month = "",
              year = ""
            )
          )

        result.errors must contain only FormError(
          "value.day",
          invalidCharactersKey
        )
      }
    }

    "Incomplete date" - {

      "must return the day required error when only the day is missing" in {

        val result =
          form.bind(
            dateData(
              day = "",
              month = "3",
              year = "1980"
            )
          )

        result.errors must contain only FormError(
          "value.day",
          dayRequiredKey
        )
      }

      "must return the month required error when only the month is missing" in {

        val result =
          form.bind(
            dateData(
              day = "31",
              month = "",
              year = "1980"
            )
          )

        result.errors must contain only FormError(
          "value.month",
          monthRequiredKey
        )
      }

      "must return the year required error when only the year is missing" in {

        val result =
          form.bind(
            dateData(
              day = "31",
              month = "3",
              year = ""
            )
          )

        result.errors must contain only FormError(
          "value.year",
          yearRequiredKey
        )
      }

      "must return the day and month required error when day and month are missing" in {

        val result =
          form.bind(
            dateData(
              day = "",
              month = "",
              year = "1980"
            )
          )

        result.errors must contain only FormError(
          "value.day",
          dayMonthRequiredKey
        )
      }

      "must return the day and year required error when day and year are missing" in {

        val result =
          form.bind(
            dateData(
              day = "",
              month = "3",
              year = ""
            )
          )

        result.errors must contain only FormError(
          "value.day",
          dayYearRequiredKey
        )
      }

      "must return the month and year required error when month and year are missing" in {

        val result =
          form.bind(
            dateData(
              day = "31",
              month = "",
              year = ""
            )
          )

        result.errors must contain only FormError(
          "value.month",
          monthYearRequiredKey
        )
      }
    }

    "Not a real date" - {

      "must return an error against day when only the day is invalid" in {

        val result =
          form.bind(
            dateData(
              day = "32",
              month = "9",
              year = "2000"
            )
          )

        result.errors must contain only FormError(
          "value.day",
          realDateKey
        )
      }

      "must return an error against month when only the month is invalid" in {

        val result =
          form.bind(
            dateData(
              day = "20",
              month = "13",
              year = "2000"
            )
          )

        result.errors must contain only FormError(
          "value.month",
          realDateKey
        )
      }

      "must return an error against the whole date when multiple components are invalid" in {

        val result =
          form.bind(
            dateData(
              day = "32",
              month = "13",
              year = "2024"
            )
          )

        result.errors must contain only FormError(
          "value",
          realDateKey
        )
      }

      "must return an error against the whole date when the combination is not a real date" in {

        val result =
          form.bind(
            dateData(
              day = "30",
              month = "2",
              year = "2024"
            )
          )

        result.errors must contain only FormError(
          "value",
          realDateKey
        )
      }

      "must treat an unrecognised alphabetic month as not a real date" in {

        val result =
          form.bind(
            dateData(
              day = "1",
              month = "NotAMonth",
              year = "2000"
            )
          )

        result.errors must contain only FormError(
          "value.month",
          realDateKey
        )
      }
    }

    "Too far in the past" - {

      "must return an error for a date before 1900" in {

        val result =
          form.bind(
            dateData(
              day = "31",
              month = "12",
              year = "1899"
            )
          )

        result.errors must contain only FormError(
          "value",
          pastKey
        )
      }

      "must accept 1 January 1900" in {

        val result =
          form.bind(
            dateData(
              day = "1",
              month = "1",
              year = "1900"
            )
          )

        result.value mustBe Some(
          IndividualDateOfBirth(
            LocalDate.of(1900, 1, 1)
          )
        )

        result.errors mustBe empty
      }
    }

    "In the future" - {

      "must return the future date error containing today's date" in {

        val today =
          LocalDate.now()

        val tomorrow =
          today.plusDays(1)

        val displayFormatter =
          DateTimeFormatter.ofPattern(
            "d MMMM yyyy",
            Locale.UK
          )

        val result =
          form.bind(
            dateData(
              day = tomorrow.getDayOfMonth.toString,
              month = tomorrow.getMonthValue.toString,
              year = tomorrow.getYear.toString
            )
          )

        result.errors must contain only FormError(
          "value",
          futureKey,
          Seq(today.format(displayFormatter))
        )
      }

      "must accept today's date" in {

        val today =
          LocalDate.now()

        val result =
          form.bind(
            dateData(
              day = today.getDayOfMonth.toString,
              month = today.getMonthValue.toString,
              year = today.getYear.toString
            )
          )

        result.value mustBe Some(
          IndividualDateOfBirth(today)
        )

        result.errors mustBe empty
      }
    }

    "filling the form" - {

      "must unbind an existing date into day, month and year fields" in {

        val answer =
          IndividualDateOfBirth(
            LocalDate.of(1980, 3, 31)
          )

        val filledForm =
          form.fill(answer)

        filledForm("value.day").value mustBe Some("31")
        filledForm("value.month").value mustBe Some("3")
        filledForm("value.year").value mustBe Some("1980")
      }
    }
  }
}
