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
import models.response.{Address, AddressLookup, Country}
import pages.*
import pages.manual.account.HaveNumberPage
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.*

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
      implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")

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

        "must go to JourneyRecovery Page when Normal Mode when answer is missing" in {
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
        "must go to UNDERCONSTRUCTION when user selects false" in {
          val ua = UserAnswers("id").withPage(IsSponsorBasedInUKPage(), false)
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
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
        val addressLookup: AddressLookup =
          AddressLookup(200000706253L,
                        Some("1 Address line 1 Road"),
                        None,
                        Some("Address line 2 Road"),
                        None,
                        "Town",
                        Some("County"),
                        "zz11zz",
                        Some(Country.GB)
          )

        "must go to IsThisAddressForSponsor when one address is found" in {
          val address: Seq[AddressLookup] = Seq(addressLookup)

          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ").withPage(AddressLookupPage(), address)
          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.IsThisAddressForSponsorController.onPageLoad(NormalMode)
        }
        "must go to WhatIsAddressForSponsor when multiple addresses are found" in {
          val addresses: Seq[AddressLookup] = Seq(addressLookup, addressLookup)

          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ").withPage(AddressLookupPage(), addresses)
          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.WhatIsAddressForSponsorController.onPageLoad(NormalMode)
        }
        "must go to ProblemPage when no addresses are found" in {
          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ").withPage(AddressLookupPage(), Seq.empty)

          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
        "must go to JourneyRecovery Page when AddressLookupPage is missing" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "WhatTypeOfFilerPage" - {
        "must go to FilerCategoryCheckAnswers" in {
          val ua = UserAnswers("id")
          navigator.nextPage(WhatTypeOfFilerPage(), NormalMode, ua) mustBe
            controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
        }
      }

      "WhatTypeOfFilerIsSponsorPage" - {
        "must go to FilerCategoryCheckAnswers" in {
          val ua = UserAnswers("id")
          navigator.nextPage(WhatTypeOfFilerIsSponsorPage(), NormalMode, ua) mustBe
            controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
        }
      }

      "HaveNumberPage" - {
        "must go to NumberType Page when answer is Yes" in {
          val userData = UserAnswers("id").withPage(HaveNumberPage(), true)
          navigator.nextPage(HaveNumberPage(), NormalMode, userData) mustBe
            controllers.manual.account.routes.NumberTypeController.onPageLoad(NormalMode)
        }

        "must go to UnderConstruction Page when when answer is No" in {
          val userData = UserAnswers("id").withPage(HaveNumberPage(), false)
          navigator.nextPage(HaveNumberPage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }

        "must go to JourneyRecovery Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveNumberPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "NumberTypePage" - {

        "must go to UnderConstruction Page when when answer is No" in {
          val userData = UserAnswers("id").withPage(NumberTypePage(), NumberType.Iban)
          navigator.nextPage(NumberTypePage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }

      }

      "WhatIsAddressForSponsor" - {
        "must go to UnderConstruction Page" in {
          val address: Address =
            Address(None, "1 Address line 1 Road", None, "Address line 2 Road", Some("Town"), Some("zz11zz"), Country.GB)
          val userData = UserAnswers("id").withPage(WhatIsAddressForSponsorPage(), address)

          navigator.nextPage(NumberTypePage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }
      }
      "IsThisAddressForSponsorPage" - {
        "must go to UnderConstruction Page" in {
          val userData = UserAnswers("id").withPage(IsThisAddressForSponsorPage(), true)

          navigator.nextPage(NumberTypePage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }

      }
    }
  }
}
