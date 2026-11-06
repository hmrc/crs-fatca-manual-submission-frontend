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

import base.SpecBase
import models.NumberType.*
import models.SubmissionsConstants.{CRS, FATCA}
import models.TypeOfReport.Information
import models.manual.account.WasAccountOpen
import models.manual.account.WhatAccountType.Custodial
import models.manual.accountHolders.SelfCertification.Yes
import models.manual.accountHolders.{
  EquityInterestType,
  EquityInterestTypes,
  IndividualDateOfBirth,
  IndividualName,
  IndividualOrOrganisation as HolderType,
  IndividualPlaceOfBirth,
  OrganisationCrs,
  OrganisationFatca
}
import models.manual.filercategory.{WhatTypeOfFiler, WhatTypeOfFilerIsSponsor}
import models.viewModels.{AccountHolderId, AccountId}
import models.{AccountBalance, CrsOrFatca, Currency, NumberType, ReportId, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import pages.manual.account.*
import pages.manual.accountHolders.{
  AccountHolderIndividualNamePage,
  AccountHolderOrganisationNamePage,
  CurrentAccountHolderIdPage as CurrentAccountHolderIdForHolderPage,
  EquityInterestTypePage,
  IndividualDateOfBirthPage,
  IndividualHaveDateOfBirthPage,
  IndividualHavePlaceOfBirthPage,
  IndividualOrOrganisationPage,
  IndividualPlaceOfBirthPage,
  OrganisationCrsPage,
  OrganisationFatcaPage,
  ProvideEquityInterestTypePage,
  SelfCertificationPage
}
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.manual.account.*
import viewmodels.checkAnswers.manual.accountHolders.*
import viewmodels.govuk.all.{FluentSummaryList, SummaryListViewModel}

import java.time.LocalDate

class CheckAnswersUtilSpec extends SpecBase {

  val util = new CheckAnswersUtil()

  implicit val messages: Messages = stubMessages()

  "ReportDetailsCheckAnswersUtil" - {

    "getReportDetailsRows" - {

      "must return an empty SummaryList when no relevant answers exist in UserAnswers" in {
        val result = util.getReportDetailsRows(emptyUserAnswers)

        result.rows mustBe Seq.empty
      }

      "must return a populated SummaryList when matching data is present in UserAnswers" in {
        val ua = emptyUserAnswers
          .withPage(CrsOrFatcaPage, CrsOrFatca.Crs)
          .withPage(ReportingYearPage, 2026)
          .withPage(TypeOfReportPage, Information)

        val result = util.getReportDetailsRows(ua)

        result.rows.size mustBe 3
      }
    }

    "getFilerCategoryRows" - {

      implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

      "must return an empty SummaryList when no relevant answers exist in UserAnswers" in {
        val result = util.getFilerCategoryRows(emptyUserAnswers)

        result.rows mustBe Seq.empty
      }

      "must return a populated SummaryList when matching data is present in UserAnswers" in {
        val ua = emptyUserAnswers
          .withPage(WhatTypeOfFilerIsSponsorPage(), WhatTypeOfFilerIsSponsor.Trustee)
          .withPage(WhatTypeOfFilerPage(), WhatTypeOfFiler.Foreign)

        val result = util.getFilerCategoryRows(ua)

        result.rows.size mustBe 2
      }
    }

    "getAccountRows" - {

      val accId = AccountId("accountId123")

      "when the regime is CRS" - {

        implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

        def baseUa(hasNumber: Boolean, isClosed: Boolean, isJointAcc: Boolean): UserAnswers =
          emptyUserAnswers
            .withPage(CurrentAccountIdPage(), accId)
            .withPage(HaveNumberPage(accId), hasNumber)
            .withPage(AccountClosedPage(accId), isClosed)
            .withPage(IsJointAccountPage(accId), isJointAcc)

        "when no relevant answers exist" - {
          "must return an empty SummaryList" in {
            util.getAccountRows(emptyUserAnswers).rows mustBe Seq.empty
            util.getAccountRows(emptyUserAnswers.withPage(CurrentAccountIdPage(), accId)).rows mustBe Seq.empty
          }
        }

        "when there are joint account holders" - {
          "must include the joint account holders summary" in {
            val ua = baseUa(hasNumber = false, isClosed = false, isJointAcc = true)
              .withPage(HowManyJointAccountHoldersPage(accId), 42)

            val expected = HaveNumberSummary.row(ua) ++
              NumberTypeSummary.row(ua) ++
              WhatIsAccountNumberSummary.row(ua) ++
              AccountClosedSummary.row(ua) ++
              WhatWasTheAccountBalanceSummary.row(ua) ++
              IsUndocumentedAccountSummary.row(ua) ++
              IsDormantAccountSummary.row(ua) ++
              WasAccountOpenSummary.row(ua) ++
              IsJointAccountSummary.row(ua) ++
              HowManyJointAccountHoldersSummary.row(ua) ++
              WhatAccountTypeSummary.row(ua)

            util.getAccountRows(ua).rows mustBe expected
          }
        }

        "when the account is not closed" - {

          "when there is no official number" - {
            "must return the correct rows, including the payments row when payments answers exist" in {
              val ua = baseUa(hasNumber = false, isClosed = false, isJointAcc = false).withPage(HavePaymentsPage(accId), true)

              val expected = HaveNumberSummary.row(ua) ++
                IdentifierSummary.row(ua) ++
                AccountClosedSummary.row(ua) ++
                WhatWasTheAccountBalanceSummary.row(ua) ++
                IsUndocumentedAccountSummary.row(ua) ++
                IsDormantAccountSummary.row(ua) ++
                WasAccountOpenSummary.row(ua) ++
                IsJointAccountSummary.row(ua) ++
                WhatAccountTypeSummary.row(ua) ++
                HavePaymentsSummary.row(ua)

              util.getAccountRows(ua).rows mustBe expected
            }

            "must omit the payments row when payments answers are absent" in {
              val ua = baseUa(hasNumber = false, isClosed = false, isJointAcc = false)

              val expected = HaveNumberSummary.row(ua) ++
                IdentifierSummary.row(ua) ++
                AccountClosedSummary.row(ua) ++
                WhatWasTheAccountBalanceSummary.row(ua) ++
                IsUndocumentedAccountSummary.row(ua) ++
                IsDormantAccountSummary.row(ua) ++
                WasAccountOpenSummary.row(ua) ++
                IsJointAccountSummary.row(ua) ++
                WhatAccountTypeSummary.row(ua)

              util.getAccountRows(ua).rows mustBe expected
            }
          }

          "when the number type is unanswered but there is no official number" - {
            "must still return rows, with the account type row (the number type is only read when there is an official number)" in {
              val ua = emptyUserAnswers
                .withPage(CurrentAccountIdPage(), accId)
                .withPage(HaveNumberPage(accId), false)
                .withPage(AccountClosedPage(accId), false)
                .withPage(IsJointAccountPage(accId), false)
                .withPage(WhatWasTheAccountBalancePage(accId), AccountBalance(currency = Currency.GBP, amount = "300"))
                .withPage(IsUndocumentedAccountPage(accId), false)
                .withPage(IsDormantAccountPage(accId), false)
                .withPage(WasAccountOpenPage(accId), WasAccountOpen.NotReported)
                .withPage(WhatAccountTypePage(accId), Custodial)

              val expected = HaveNumberSummary.row(ua) ++
                IdentifierSummary.row(ua) ++
                AccountClosedSummary.row(ua) ++
                WhatWasTheAccountBalanceSummary.row(ua) ++
                IsUndocumentedAccountSummary.row(ua) ++
                IsDormantAccountSummary.row(ua) ++
                WasAccountOpenSummary.row(ua) ++
                IsJointAccountSummary.row(ua) ++
                WhatAccountTypeSummary.row(ua)

              util.getAccountRows(ua).rows mustBe expected
            }
          }

          "when IsJointAccountPage is unanswered" - {
            "must still return rows (the joint account answer no longer short-circuits the comprehension)" in {
              val ua = emptyUserAnswers
                .withPage(CurrentAccountIdPage(), accId)
                .withPage(HaveNumberPage(accId), true)
                .withPage(AccountClosedPage(accId), false)
                .withPage(NumberTypePage(accId), NumberType.Other)
                .withPage(WhatIsAccountNumberPage(accId, reportId), "TESTACCNO123")
                .withPage(WhatWasTheAccountBalancePage(accId), AccountBalance(currency = Currency.GBP, amount = "300"))
                .withPage(IsUndocumentedAccountPage(accId), false)
                .withPage(IsDormantAccountPage(accId), false)
                .withPage(WasAccountOpenPage(accId), WasAccountOpen.NotReported)
                .withPage(WhatAccountTypePage(accId), Custodial)

              val expected = HaveNumberSummary.row(ua) ++
                NumberTypeSummary.row(ua) ++
                WhatIsAccountNumberSummary.row(ua) ++
                AccountClosedSummary.row(ua) ++
                WhatWasTheAccountBalanceSummary.row(ua) ++
                IsUndocumentedAccountSummary.row(ua) ++
                IsDormantAccountSummary.row(ua) ++
                WasAccountOpenSummary.row(ua) ++
                WhatAccountTypeSummary.row(ua)

              util.getAccountRows(ua).rows mustBe expected
            }
          }

          "when there is an official number" - {
            "must return the correct rows" in {
              val ua = baseUa(hasNumber = true, isClosed = false, isJointAcc = false).withPage(NumberTypePage(accId), Other)

              val expected = HaveNumberSummary.row(ua) ++
                NumberTypeSummary.row(ua) ++
                WhatIsAccountNumberSummary.row(ua) ++
                AccountClosedSummary.row(ua) ++
                WhatWasTheAccountBalanceSummary.row(ua) ++
                IsUndocumentedAccountSummary.row(ua) ++
                IsDormantAccountSummary.row(ua) ++
                WasAccountOpenSummary.row(ua) ++
                IsJointAccountSummary.row(ua) ++
                WhatAccountTypeSummary.row(ua)

              util.getAccountRows(ua).rows mustBe expected
            }

          }

        }

        "when the account is closed" - {
          "must show the currency row but not the balance row" in {
            val ua = baseUa(hasNumber = false, isClosed = true, isJointAcc = false)

            val expected = HaveNumberSummary.row(ua) ++
              NumberTypeSummary.row(ua) ++
              WhatIsAccountNumberSummary.row(ua) ++
              AccountClosedSummary.row(ua) ++
              WhatWasTheAccountCurrencySummary.row(ua) ++
              IsUndocumentedAccountSummary.row(ua) ++
              IsDormantAccountSummary.row(ua) ++
              WasAccountOpenSummary.row(ua) ++
              IsJointAccountSummary.row(ua) ++
              WhatAccountTypeSummary.row(ua)

            util.getAccountRows(ua).rows mustBe expected
          }
        }

        "when the number type is Iban" - {
          "must omit the account type row" in {
            val ua = baseUa(hasNumber = true, isClosed = false, isJointAcc = false)
              .withPage(NumberTypePage(accId), Iban)

            val result = util.getAccountRows(ua)

            result.rows must not contain (WhatAccountTypeSummary.row(ua))
          }
        }

        "when the number type is Semp" - {
          "must omit the account type row" in {
            val ua = baseUa(hasNumber = true, isClosed = true, isJointAcc = false)
              .withPage(NumberTypePage(accId), Semp)

            val result = util.getAccountRows(ua)

            result.rows must not contain (WhatAccountTypeSummary.row(ua))
          }
        }
      }

      "when the regime is FATCA" - {

        implicit val reportId: ReportId = ReportId(FATCA, 2025, None, "TestfiID")

        def baseUa(hasNumber: Boolean, isClosed: Boolean, isJointAcc: Boolean): UserAnswers =
          emptyUserAnswers
            .withPage(CurrentAccountIdPage(), accId)
            .withPage(HaveNumberPage(accId), hasNumber)
            .withPage(AccountClosedPage(accId), isClosed)
            .withPage(IsJointAccountPage(accId), isJointAcc)

        "when no relevant answers exist" - {
          "must return an empty SummaryList" in {
            util.getAccountRows(emptyUserAnswers).rows mustBe Seq.empty
            util.getAccountRows(emptyUserAnswers.withPage(CurrentAccountIdPage(), accId)).rows mustBe Seq.empty
          }
        }

        "when the account is not closed" - {
          "must show the balance but no CRS-only rows" in {
            val ua = baseUa(hasNumber = false, isClosed = false, isJointAcc = false)

            val expected = HaveNumberSummary.row(ua) ++
              IdentifierSummary.row(ua) ++
              AccountClosedSummary.row(ua) ++
              WhatWasTheAccountBalanceSummary.row(ua)

            util.getAccountRows(ua).rows mustBe expected
          }

        }

        "when the account is closed" - {
          "must show the balance" in {
            val ua = baseUa(hasNumber = false, isClosed = false, isJointAcc = false)

            val expected = HaveNumberSummary.row(ua) ++
              NumberTypeSummary.row(ua) ++
              WhatIsAccountNumberSummary.row(ua) ++
              AccountClosedSummary.row(ua) ++
              WhatWasTheAccountBalanceSummary.row(ua)

            util.getAccountRows(ua).rows mustBe expected
          }
        }
      }
    }

    "getAccountHolderRows" - {

      implicit val accountHolderId: AccountHolderId = AccountHolderId("holder-id")

      def getIndividualAnswers(implicit reportId: ReportId): UserAnswers =
        emptyUserAnswers
          .withPage(CurrentAccountHolderIdForHolderPage()(reportId), accountHolderId)
          .withPage(IndividualOrOrganisationPage(accountHolderId)(reportId), HolderType.Individual)

      def getOrganisationAnswers(implicit reportId: ReportId): UserAnswers =
        emptyUserAnswers
          .withPage(CurrentAccountHolderIdForHolderPage()(reportId), accountHolderId)
          .withPage(IndividualOrOrganisationPage(accountHolderId)(reportId), HolderType.Organisation)

      def expected(ua: UserAnswers, rows: Iterable[SummaryListRow]): Seq[SummaryList] =
        Seq(SummaryListViewModel(rows = rows.toSeq).withCssClass("govuk-!-margin-bottom-3")) ++
          AccountHolderTaxResidentCountriesSummary.row(ua)

      "when the regime is CRS" - {

        implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

        "when the holder is an individual" - {

          "must return the individual summaries in the expected order" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              IndividualNameSummary.row(ua) ++
              IndividualHaveDateOfBirthSummary.row(ua) ++
              IndividualDateOfBirthSummary.row(ua) ++
              IndividualHavePlaceOfBirthSummary.row(ua) ++
              IndividualPlaceOfBirthSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }

          "when the IndividualHaveDateOfBirthPage is no" - {

            "must show the date of birth answer and hide the date of birth" in {
              val ua = getIndividualAnswers
                .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), false)
                .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))

              val expectedRows = AccountsSummary.row(ua) ++
                IndividualOrOrganisationSummary.row(ua) ++
                IndividualHaveDateOfBirthSummary.row(ua) ++
                AddressForAccountHolderSummary.row(ua)

              util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
            }
          }

          "when the IndividualHaveDateOfBirthPage is yes" - {

            "must show the date of birth answer and hide the date of birth" in {
              val ua = getIndividualAnswers
                .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
                .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))

              val expectedRows = AccountsSummary.row(ua) ++
                IndividualOrOrganisationSummary.row(ua) ++
                IndividualHaveDateOfBirthSummary.row(ua) ++
                IndividualDateOfBirthSummary.row(ua) ++
                AddressForAccountHolderSummary.row(ua)

              util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
            }
          }

          "when the IndividualHavePlaceOfBirthPage is no" - {
            "must show the place of birth boolean row" in {
              val ua = getIndividualAnswers
                .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), false)
                .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

              val expectedRows = AccountsSummary.row(ua) ++
                IndividualOrOrganisationSummary.row(ua) ++
                IndividualHavePlaceOfBirthSummary.row(ua) ++
                AddressForAccountHolderSummary.row(ua)

              util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
            }
          }

          "when the IndividualHavePlaceOfBirthPage is yes" - {
            "must show the place of birth rows" in {
              val ua = getIndividualAnswers
                .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
                .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

              val expectedRows = AccountsSummary.row(ua) ++
                IndividualOrOrganisationSummary.row(ua) ++
                IndividualHavePlaceOfBirthSummary.row(ua) ++
                IndividualPlaceOfBirthSummary.row(ua) ++
                AddressForAccountHolderSummary.row(ua)

              util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
            }
          }

          "must not render any organisation summaries" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(OrganisationFatcaPage(accountHolderId)(reportId), OrganisationFatca.SpecifiedPerson)
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), true)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }
        }

        "when the holder is an organisation" - {

          "must return the organisation summaries with the CRS row" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(OrganisationFatcaPage(accountHolderId)(reportId), OrganisationFatca.SpecifiedPerson)
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), true)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))
              .withPage(SelfCertificationPage(accountHolderId, reportId), Yes)

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              OrganisationNameSummary.row(ua) ++
              OrganisationCrsSummary.row(ua) ++
              ProvideEquityInterestTypeSummary.row(ua) ++
              EquityInterestTypeSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua) ++
              SelfCertificationSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }

          "must render the name row even when the regime specific row is unanswered" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              OrganisationNameSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }

          "when the ProvideEquityInterestTypePage is no" - {

            val ua = getOrganisationAnswers
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), false)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              ProvideEquityInterestTypeSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)

          }

          "must not render any individual summaries" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }
        }
      }

      "when the regime is FATCA" - {

        implicit val reportId: ReportId = ReportId(FATCA, 2025, None, "TestfiID")

        "when the holder is an organisation" - {

          "must use the FATCA row instead of the CRS row" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(OrganisationFatcaPage(accountHolderId)(reportId), OrganisationFatca.SpecifiedPerson)

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              OrganisationNameSummary.row(ua) ++
              OrganisationFatcaSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }
        }

        "when the holder is an individual" - {

          "must return the individual summaries" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              IndividualNameSummary.row(ua) ++
              IndividualHaveDateOfBirthSummary.row(ua) ++
              IndividualDateOfBirthSummary.row(ua) ++
              IndividualHavePlaceOfBirthSummary.row(ua) ++
              IndividualPlaceOfBirthSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }
        }
      }
    }
  }
}
