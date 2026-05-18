package models

import base.SpecBase
import pages.{CarfGrossProceedsPage, CrsGrossProceedsPage}

class UserDataSpec extends SpecBase {

  "CarfGrossProceedsPage" - {
    "must remove CrsGrossProceedsPage when CarfGrossProceedsPage is set to false" in {
      val userData = emptyUserAnswers
        .withPage(CrsGrossProceedsPage, true)
        .withPage(CarfGrossProceedsPage, false)

      userData.get(CrsGrossProceedsPage) mustBe None
    }

    "must not remove CrsGrossProceedsPage when CarfGrossProceedsPage is set to true" in {
      val userData = emptyUserAnswers
        .withPage(CrsGrossProceedsPage, true)
        .withPage(CarfGrossProceedsPage, true)

      userData.get(CrsGrossProceedsPage) mustBe Some(true)
    }
  }
}
