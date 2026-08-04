package models.manual.sponsor

import base.SpecBase
import models.sponsor.RemoveCountryMessage
import models.sponsor.RemoveCountryMessage.{AllOtherCountryMessage, NationsWithDefiniteArticlesMessage, OtherCountryMessage}

class RemoveCountryMessageSpec extends SpecBase {
  
  "RemoveCountryMessage" - {
    "return correct enum given a code" in {
      RemoveCountryMessage.getRemoveCountryMessage("XX") mustEqual OtherCountryMessage

      definiteArticleCountries.foreach(c =>
        RemoveCountryMessage.getRemoveCountryMessage(c) mustEqual NationsWithDefiniteArticlesMessage
      )

      RemoveCountryMessage.getRemoveCountryMessage("ZW") mustEqual AllOtherCountryMessage
      
    }
  }

  def definiteArticleCountries = Seq("CC",
    "CF",
    "DO",
    "MH",
    "AE",
    "GB",
    "UM",
    "US",
    "VI",
    "AX",
    "CG",
    "CG",
    "CK",
    "FK",
    "FO",
    "GS",
    "HM",
    "IO",
    "KY",
    "MP",
    "NL",
    "PH",
    "PN",
    "PS",
    "SB",
    "TC",
    "TF",
    "VG",
    "WF",
    "IM"
  )
}
