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

package models.manual.account

import base.SpecBase
import models.NumberType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.ReportingConstants.REPORTING_THRESHOLD_YEAR

class WhatAccountTypeSpec extends SpecBase {

  implicit val messages: Messages = MessagesImpl(Lang.defaultLang, new DefaultMessagesApi)

  "WhatAccountType" - {

    "must deserialise every base value from its string" in {
      WhatAccountType.baseValues.foreach {
        whatAccountType =>
          JsString(whatAccountType.toString).validate[WhatAccountType].asOpt.value mustEqual whatAccountType
      }
    }

    "must fail to deserialise values that are not in the base set" in {
      val invalid = Seq("InsuranceOrAnnuityContract", "NotReported", "invalid")

      invalid.foreach {
        invalidValue =>
          JsString(invalidValue).validate[WhatAccountType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise every base value back to its string" in {
      WhatAccountType.baseValues.foreach {
        whatAccountType =>
          Json.toJson(whatAccountType) mustEqual JsString(whatAccountType.toString)
      }
    }

    "must be unchanged for toJson then fromJson for every base value" in {
      WhatAccountType.baseValues.foreach {
        whatAccountType =>
          Json.fromJson[WhatAccountType](Json.toJson(whatAccountType)) mustEqual JsSuccess(whatAccountType)
      }
    }
  }

  "WhatAccountType.options" - {

    def optionValues(items: Seq[RadioItem]): Seq[String] =
      items.flatMap(_.value)

    "must add InsuranceOrAnnuityContract to the options when numberType is Other and the reporting period is at or after the threshold year" in {
      val items = WhatAccountType.options(NumberType.Other, REPORTING_THRESHOLD_YEAR)

      optionValues(items) mustBe
        Seq("Depository", "Custodial", "InvestmentEntity", "InsuranceOrAnnuityContract")
    }

    "must add both InsuranceOrAnnuityContract and NotReported to the options when numberType is Other and the reporting period is before the threshold year" in {
      val items = WhatAccountType.options(NumberType.Other, REPORTING_THRESHOLD_YEAR - 1)

      optionValues(items) mustBe
        Seq("Depository", "Custodial", "InvestmentEntity", "InsuranceOrAnnuityContract", "NotReported")
    }

    "must add NotReported to the options when the reporting period is before the threshold year and numberType is not Other" in {
      val items = WhatAccountType.options(NumberType.Iban, REPORTING_THRESHOLD_YEAR - 1)

      optionValues(items) mustBe
        Seq("Depository", "Custodial", "InvestmentEntity", "NotReported")
    }

    "must offer only the base options when the reporting period is at or after the threshold year and numberType is not Other" in {
      val items = WhatAccountType.options(NumberType.Iban, REPORTING_THRESHOLD_YEAR)

      optionValues(items) mustBe Seq("Depository", "Custodial", "InvestmentEntity")
    }

    "must build each radio option with the message key, value and index-based id" in {
      val items = WhatAccountType.options(NumberType.Other, REPORTING_THRESHOLD_YEAR)

      items.zipWithIndex.foreach {
        case (item, index) =>
          val expectedValue = optionValues(Seq(item)).head
          item.content mustEqual Text(messages(s"whatAccountType.$expectedValue"))
          item.id mustBe Some(s"value_$index")
      }
    }
  }

}
