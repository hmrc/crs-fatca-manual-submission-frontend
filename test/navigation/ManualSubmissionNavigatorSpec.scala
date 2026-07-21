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

package navigation

import base.SpecBase
import controllers.manual.reportdetails.routes.{ReportDetailsCheckAnswersController, ReportingYearController, TypeOfReportController}
import models.*
import models.SubmissionsConstants.FATCA
import pages.*
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.{HaveSponsorPage, IsSponsorBasedInUKPage, SponsorNamePage, UKPostcodePage, WhatIsGIINForSponsorPage}

class ManualSubmissionNavigatorSpec extends SpecBase {

  val navigator = new ManualSubmissionNavigator

  "ManualSubmissionNavigator in NormalMode" - {
    "nextPageWithoutReportId" - {
      "CrsOrFatcaPage" - {
        "must go to Reporting Year Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPageWithoutReportId(CrsOrFatcaPage, NormalMode, userData) mustBe
            ReportingYearController.onPageLoad(NormalMode)
        }

      }
      "ReportingYearPage" - {
        "must go to TypeOfReport Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPageWithoutReportId(ReportingYearPage, NormalMode, userData) mustBe
            TypeOfReportController.onPageLoad(NormalMode)
        }

      }
      "TypeOfReportPage" - {
        "must go to ReportDetailsCheckAnswers" in {
          val ua = UserAnswers("id")
          navigator.nextPageWithoutReportId(TypeOfReportPage, NormalMode, ua) mustBe
            ReportDetailsCheckAnswersController.onPageLoad()
        }

      }
    }
    "with reportId" - {
      implicit val reportId = ReportId(FATCA, 2024, None, "TestFIID")

      "HaveSponsorPage" - {
        "must go to SponsorName Page when answer is Yes" in {
          val userData = UserAnswers("id").withPage(HaveSponsorPage(), true)
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.SponsorNameController.onPageLoad(NormalMode)
        }

        "must go to UnderConstruction Page when when answer is No" in {
          val userData = UserAnswers("id").withPage(HaveSponsorPage(), false)
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }

        "must go to JourneyRecovery Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "SponsorNamePage" - {
        "must go to WhatIsGIINForSponsor Page" in {
          val userData = UserAnswers("id")
          navigator.nextPage(SponsorNamePage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(NormalMode)
        }
      }

      "WhatIsGIINForSponsorPage" - {
        "must go to IsSponsorBasedInUK" in {
          val ua = UserAnswers("id")
          navigator.nextPage(WhatIsGIINForSponsorPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(NormalMode)
        }
      }

      "IsSponsorBasedInUKPage" - {
        "must go to Non UK Address page when user selects false" in {
          val ua = UserAnswers("id").withPage(IsSponsorBasedInUKPage(), false)
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.AddressNonUkController.onPageLoad(NormalMode)
        }

        "must go to UK Postcode when user selects true" in {
          val ua = UserAnswers("id").withPage(IsSponsorBasedInUKPage(), true)
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(NormalMode)
        }

        "must go to There is a problem page when no answer available" in {
          val ua = UserAnswers("id")
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "UKPostcodePage" - {
        "must go to UNDERCONSTRUCTION when user selects false" in {
          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ")
          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }
      }

      "AddressNonUkPage" - {
        "must go to UNDERCONSTRUCTION page after user hits submit" in {
          val ua = UserAnswers("id")
          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }
      }
    }
  }
}
