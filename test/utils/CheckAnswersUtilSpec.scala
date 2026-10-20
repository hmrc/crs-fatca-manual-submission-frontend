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
import models.manual.filercategory.{WhatTypeOfFiler, WhatTypeOfFilerIsSponsor}
import models.viewModels.AccountId
import models.{AccountBalance, CrsOrFatca, Currency, NumberType, ReportId, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import pages.manual.account.*
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.checkAnswers.manual.account.*

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

  }
}
