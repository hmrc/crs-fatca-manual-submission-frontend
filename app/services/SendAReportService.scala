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

package services

import models.CrsOrFatca.reads
import models.viewModels.TaskStatus.{CannotStartYet, Completed, Incomplete, NotStarted}
import models.viewModels.{AccountId, TaskStatus}
import models.{viewModels, ReportId, UserAnswers}
import pages.QuestionPage
import pages.manual.account.{AccountsPage, IsAccountsSectionCompletedPage}
import pages.manual.accountHolders.IsAccountHoldersSectionCompletePage
import pages.manual.cpso.IsCpsoSectionCompletePage
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportingFI.HaveYouFinishedCheckingFIDetailsPage
import pages.manual.sponsor.*
import services.manual.account.AccountsService

import javax.inject.Inject

class SendAReportService @Inject() (accountsService: AccountsService,cpsoService: CpsoService, accountHolderService: AccountHolderService) {

  def reportingFIStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus =
    userAnswers
      .get(HaveYouFinishedCheckingFIDetailsPage(reportId))
      .fold(Incomplete) {
        value => if value then Completed else Incomplete
      }

  def reportingCPSOCompleteStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus =
    userAnswers
      .get(IsCpsoSectionCompletePage(reportId))
      .fold(Incomplete) {
        value => if value && cpsoService.areAllCpsosComplete(userAnswers, reportId) then Completed else Incomplete
      }

  def accountHoldersCompleteStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus = {
    val noAccountHoldersAdded: Boolean     = accountHolderService.accountHolders(userAnswers)(reportId).isEmpty
    val sectionIsDeclaredComplete: Boolean = userAnswers.get(IsAccountHoldersSectionCompletePage(reportId)).contains(true)
    val accountHoldersAreComplete: Boolean = accountHolderService.areAllAccountHoldersComplete(userAnswers, reportId)

    if (noAccountHoldersAdded) NotStarted
    else if (sectionIsDeclaredComplete && accountHoldersAreComplete) Completed
    else Incomplete

  }

  def fillerStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus =
    userAnswers.get(HaveSponsorPage()(reportId)).fold[TaskStatus](CannotStartYet) {
      hasSponsor =>
        if hasSponsor then
          userAnswers
            .get(WhatTypeOfFilerIsSponsorPage()(reportId))
            .fold(Incomplete)(
              _ => Completed
            )
        else
          userAnswers
            .get(WhatTypeOfFilerPage()(reportId))
            .fold(Incomplete)(
              _ => Completed
            )
    }

  def sponsorStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus =
    userAnswers.get(HaveSponsorPage()(reportId)).fold[TaskStatus](Incomplete) {
      case false => Completed
      case true =>
        Option
          .when(allSponsorPagesAnswered(userAnswers)(using reportId))(Completed)
          .getOrElse(Incomplete)
    }

  def accountStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus =
    getAccountStatus(userAnswers, reportId)

  private def sponsorRequiredPages(using reportId: ReportId): Seq[QuestionPage[_]] =
    List(
      SponsorNamePage(),
      WhatIsGIINForSponsorPage(),
      TaxResidentCountriesListPage()
    )

  private def sponsorAddressPages(using reportId: ReportId): Seq[QuestionPage[?]] =
    List(
      WhatIsAddressForSponsorPage(),
      UkAddressPage(),
      AddressNonUkPage()
    )

  private def pageAnswered(
    page: QuestionPage[?],
    userAnswers: UserAnswers
  ): Boolean =
    page.path.asSingleJsResult(userAnswers.data).isSuccess

  private def allSponsorPagesAnswered(
    userAnswers: UserAnswers
  )(using reportId: ReportId): Boolean = {

    val requiredPagesPresent = sponsorRequiredPages.forall(pageAnswered(_, userAnswers))

    val addressPresent = sponsorAddressPages.exists(pageAnswered(_, userAnswers))

    requiredPagesPresent && addressPresent
  }

  private def getAccountStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus = {
    val accountIds = userAnswers
      .get(AccountsPage(reportId))
      .map(_.accounts.keys.toSeq.map(AccountId.apply))
      .getOrElse(Seq.empty)

    if (accountIds.isEmpty) {
      NotStarted
    } else if (
      userAnswers.get(IsAccountsSectionCompletedPage(reportId)).contains(true) &&
      accountIds.forall {
        accountId =>
          accountsService.isComplete(
            userAnswers,
            reportId,
            accountId
          )
      }
    ) {
      Completed
    } else {
      Incomplete
    }
  }

}
