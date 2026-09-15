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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsError, JsString, Json}
import models.manual.account.PaymentType.*

class PaymentTypeSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues with GuiceOneAppPerSuite {

  implicit lazy val messages: Messages =
    app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))

  "PaymentType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(PaymentType.values.toSeq)

      forAll(gen) {
        paymentType =>
          JsString(paymentType.toString).validate[PaymentType].asOpt.value mustEqual paymentType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!PaymentType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[PaymentType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(PaymentType.values.toSeq)

      forAll(gen) {
        paymentType =>
          Json.toJson(paymentType) mustEqual JsString(paymentType.toString)
      }
    }

    "PaymentType" - {
      "must resolve the message key for each payment type" in {
        val cases: List[(PaymentType, String)] = List(
          CRSDividends                    -> "dividends",
          CRSInterest                     -> "interest",
          CRSGrossProceedsOrRedemptions   -> "gross proceeds or redemptions",
          CRSOther                        -> "other",
          FATCADividends                  -> "dividends",
          FATCAInterest                   -> "interest",
          FATCAGrossProceedsOrRedemptions -> "gross proceeds or redemptions",
          FATCAOther                      -> "other"
        )

        cases.foreach {
          case (paymentType, msg) =>
            paymentType.toMessage mustBe msg
        }

      }
    }
  }
}
