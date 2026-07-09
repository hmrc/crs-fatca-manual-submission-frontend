package models.manual


import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class WhatTypeOfFilerSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "WhatTypeOfFiler" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(WhatTypeOfFiler.values.toSeq)

      forAll(gen) {
        whatTypeOfFiler =>

          JsString(whatTypeOfFiler.toString).validate[WhatTypeOfFiler].asOpt.value mustEqual whatTypeOfFiler
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhatTypeOfFiler.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[WhatTypeOfFiler] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(WhatTypeOfFiler.values.toSeq)

      forAll(gen) {
        whatTypeOfFiler =>

          Json.toJson(whatTypeOfFiler) mustEqual JsString(whatTypeOfFiler.toString)
      }
    }
  }
}
