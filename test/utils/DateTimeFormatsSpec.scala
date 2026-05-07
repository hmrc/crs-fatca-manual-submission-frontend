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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.{dateTimeFormat, formatTimeSent, formatTimeVoidSubmitted}

import java.time.{LocalDate, LocalDateTime}

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  ".dateTimeFormat" - {
    "must format dates in English" in {
      val formatter = dateTimeFormat()(Lang("en"))
      val result    = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }

    "must default to English format" in {
      val formatter = dateTimeFormat()(Lang("de"))
      val result    = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }
  }

  ".formatTimeSent" - {
    "must format a morning datetime" in {
      LocalDateTime.of(2024, 3, 6, 9, 30).formatTimeSent mustEqual "Sent 6 March 2024 at 9:30am"
    }

    "must format an afternoon datetime" in {
      LocalDateTime.of(2024, 3, 23, 14, 30).formatTimeSent mustEqual "Sent 23 March 2024 at 2:30pm"
    }

    "must format midnight as 12am" in {
      LocalDateTime.of(2014, 3, 15, 0, 0).formatTimeSent mustEqual "Sent 15 March 2014 at 12:00am"
    }

    "must format noon as 12pm" in {
      LocalDateTime.of(2024, 3, 6, 12, 0).formatTimeSent mustEqual "Sent 6 March 2024 at 12:00pm"
    }
    "must convert UTC to BST (UTC+1)  in summer" in {
      LocalDateTime.of(2024, 5, 6, 9, 30).formatTimeSent mustEqual "Sent 6 May 2024 at 10:30am"
    }

    "must convert UTC to GMT (UTC+0) in winter" in {
      LocalDateTime.of(2024, 3, 15, 9, 30).formatTimeSent mustEqual "Sent 15 March 2024 at 9:30am"
    }
  }

  ".formatTimeVoidSubmitted" - {
    "must format a morning datetime" in {
      LocalDateTime.of(2024, 3, 15, 9, 30).formatTimeVoidSubmitted mustEqual "On 15 March 2024 at 9:30am"
    }

    "must format an afternoon datetime" in {
      LocalDateTime.of(2024, 3, 15, 14, 30).formatTimeVoidSubmitted mustEqual "On 15 March 2024 at 2:30pm"
    }
    "must convert UTC to BST (UTC+1) in summer" in {
      LocalDateTime.of(2024, 7, 1, 13, 0).formatTimeVoidSubmitted mustEqual "On 1 July 2024 at 2:00pm"
    }
  }
}
