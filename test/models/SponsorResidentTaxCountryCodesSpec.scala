package models

import base.SpecBase

class SponsorResidentTaxCountryCodesSpec extends SpecBase {

  "SponsorResidentTaxCountryCodes" - {

    "must return the correct country codes" in {
      val expectedCountryCodes = Seq(
        "GB",
        "GG",
        "JE",
        "IM"
      )

      val actualCountryCodes =  SponsorResidentTaxCountryCodes(expectedCountryCodes).getCountryCode(Some(0))

      actualCountryCodes mustEqual expectedCountryCodes.head
      
      SponsorResidentTaxCountryCodes(expectedCountryCodes).getCountryCode(Some(10)) mustEqual ""
    }
  }
}
