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

package models

import models.SubmissionsConstants.{CRS, FATCA}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.i18n.Messages
import play.api.libs.json.{JsError, JsString, Json}
import play.api.test.Helpers.stubMessages

class NumberTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "NumberType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(NumberType.values.toSeq)

      forAll(gen) {
        numberType =>
          JsString(numberType.toString).validate[NumberType].asOpt.value mustEqual numberType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!NumberType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[NumberType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(NumberType.values.toSeq)

      forAll(gen) {
        numberType =>
          Json.toJson(numberType) mustEqual JsString(numberType.toString)
      }
    }

    "options" - {

      implicit val messages: Messages = stubMessages()

      "should return options with SEMP" in {
        NumberType.options(CRS)(messages).size mustBe 6
      }

      "should return options without SEMP" in {
        NumberType.options(FATCA)(messages).size mustBe 5
      }
    }
  }
}
