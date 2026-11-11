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

        val rows = result.rows
        rows.size mustBe 3
        result.classes must include("govuk-!-margin-bottom-5")
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

        "when the account holder is an individual" - {

          "must include individual rows and CRS-only rows" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), true)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))
              .withPage(SelfCertificationPage(accountHolderId, reportId), Yes)

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              IndividualNameSummary.row(ua) ++
              IndividualHaveDateOfBirthSummary.row(ua) ++
              IndividualDateOfBirthSummary.row(ua) ++
              IndividualHavePlaceOfBirthSummary.row(ua) ++
              IndividualPlaceOfBirthSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua) ++
              ProvideEquityInterestTypeSummary.row(ua) ++
              EquityInterestTypeSummary.row(ua) ++
              SelfCertificationSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }

          "must omit date-of-birth details when the answer is no" in {
            val ua = getIndividualAnswers
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), false)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must contain(IndividualHaveDateOfBirthSummary.row(ua).value)
            rows must not contain IndividualDateOfBirthSummary.row(ua).value
          }

          "must include date-of-birth details when the answer is yes" in {
            val ua = getIndividualAnswers
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must contain(IndividualHaveDateOfBirthSummary.row(ua).value)
            rows must contain(IndividualDateOfBirthSummary.row(ua).value)
          }

          "must omit place-of-birth details when the answer is no" in {
            val ua = getIndividualAnswers
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), false)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must contain(IndividualHavePlaceOfBirthSummary.row(ua).value)
            rows must not contain IndividualPlaceOfBirthSummary.row(ua).value
          }

          "must include place-of-birth details when the answer is yes" in {
            val ua = getIndividualAnswers
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must contain(IndividualHavePlaceOfBirthSummary.row(ua).value)
            rows must contain(IndividualPlaceOfBirthSummary.row(ua).value)
          }

          "must not include organisation-specific rows" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(OrganisationFatcaPage(accountHolderId)(reportId), OrganisationFatca.SpecifiedPerson)

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must not contain OrganisationNameSummary.row(ua).value
          }
        }

        "when the account holder is an organisation" - {

          "must include the CRS type, equity-interest and self-certification rows" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), true)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))
              .withPage(SelfCertificationPage(accountHolderId, reportId), Yes)

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              OrganisationNameSummary.row(ua) ++
              AccountHolderTypeCrsSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua) ++
              ProvideEquityInterestTypeSummary.row(ua) ++
              EquityInterestTypeSummary.row(ua) ++
              SelfCertificationSummary.row(ua)

            util.getAccountHolderRows(ua) mustBe expected(ua, expectedRows)
          }

          "must omit equity-interest details when the answer is no" in {
            val ua = getOrganisationAnswers
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), false)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must contain(ProvideEquityInterestTypeSummary.row(ua).value)
            rows must not contain EquityInterestTypeSummary.row(ua).value
          }

          "must not include individual-specific rows" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must not contain IndividualNameSummary.row(ua).value
            rows must not contain IndividualHaveDateOfBirthSummary.row(ua).value
            rows must not contain IndividualHavePlaceOfBirthSummary.row(ua).value
          }
        }
      }

      "when the regime is FATCA" - {

        implicit val reportId: ReportId = ReportId(FATCA, 2025, None, "TestfiID")

        "when the account holder is an individual" - {

          "must include individual rows but omit CRS-only equity and self-certification rows" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), true)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))
              .withPage(SelfCertificationPage(accountHolderId, reportId), Yes)

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              IndividualNameSummary.row(ua) ++
              IndividualHaveDateOfBirthSummary.row(ua) ++
              IndividualDateOfBirthSummary.row(ua) ++
              IndividualHavePlaceOfBirthSummary.row(ua) ++
              IndividualPlaceOfBirthSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            val rows = util.getAccountHolderRows(ua).head.rows

            rows mustBe expectedRows
            rows must not contain ProvideEquityInterestTypeSummary.row(ua).value
            rows must not contain EquityInterestTypeSummary.row(ua).value
            rows must not contain SelfCertificationSummary.row(ua).value
          }

          "must not include organisation-specific rows" in {
            val ua = getIndividualAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(OrganisationFatcaPage(accountHolderId)(reportId), OrganisationFatca.SpecifiedPerson)

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must not contain OrganisationNameSummary.row(ua).value
          }
        }

        "when the account holder is an organisation" - {

          "must include the FATCA type but omit CRS-only equity and self-certification rows" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderOrganisationNamePage(accountHolderId)(reportId), "Organisation")
              .withPage(OrganisationFatcaPage(accountHolderId)(reportId), OrganisationFatca.SpecifiedPerson)
              .withPage(OrganisationCrsPage(accountHolderId)(reportId), OrganisationCrs.CrsReportable)
              .withPage(ProvideEquityInterestTypePage(accountHolderId, reportId), true)
              .withPage(EquityInterestTypePage(accountHolderId, reportId), EquityInterestTypes(Seq(EquityInterestType.TrusteeOfTrust)))
              .withPage(SelfCertificationPage(accountHolderId, reportId), Yes)

            val expectedRows = AccountsSummary.row(ua) ++
              IndividualOrOrganisationSummary.row(ua) ++
              OrganisationNameSummary.row(ua) ++
              AccountHolderTypeFatcaSummary.row(ua) ++
              AddressForAccountHolderSummary.row(ua)

            val rows = util.getAccountHolderRows(ua).head.rows

            rows mustBe expectedRows
            rows must not contain AccountHolderTypeCrsSummary.row(ua).value
            rows must not contain ProvideEquityInterestTypeSummary.row(ua).value
            rows must not contain EquityInterestTypeSummary.row(ua).value
            rows must not contain SelfCertificationSummary.row(ua).value
          }

          "must not include individual-specific rows" in {
            val ua = getOrganisationAnswers
              .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("First", "Last"))
              .withPage(IndividualHaveDateOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualDateOfBirthPage(accountHolderId)(reportId), IndividualDateOfBirth(LocalDate.of(1990, 1, 2)))
              .withPage(IndividualHavePlaceOfBirthPage(accountHolderId)(reportId), true)
              .withPage(IndividualPlaceOfBirthPage(accountHolderId, reportId), IndividualPlaceOfBirth(Some("City"), None, "GB"))

            val rows = util.getAccountHolderRows(ua).head.rows

            rows must not contain IndividualNameSummary.row(ua).value
            rows must not contain IndividualHaveDateOfBirthSummary.row(ua).value
            rows must not contain IndividualHavePlaceOfBirthSummary.row(ua).value
          }
        }
      }
    }
  }
}
