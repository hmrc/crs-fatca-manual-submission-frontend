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

package controllers

import base.SpecBase
import controllers.manual.routes.SendAReportController
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.TaskStatus.*
import models.viewModels.{AccountId, SendAReportSections, TaskStatus}
import models.{AccountBalance, Currency, NormalMode, ReportId}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import pages.manual.account.*
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SendAReportService
import views.html.SendAReportView

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class SendAReportControllerSpec extends SpecBase {
  "SendAReport Controller" - {
    val reportId = ReportId(CRS, 2025, None, "testFiID")
    val ua       = emptyUserAnswers.withPage(ReportIdPage, reportId)
    val sections = SendAReportSections(
      reportDetails = Some(NotStarted),
      financialInstitutionDetails = Some(NotStarted),
      sponsorDetails = Some(NotStarted),
      filerCategory = Some(NotStarted),
      accounts = Some(NotStarted),
      accountHolders = Some(NoStatus),
      controllingPersons = Some(Completed),
      tbc1 = Some(Incomplete),
      tbc2 = Some(Incomplete),
      userAnswers = emptyUserAnswers,
      reportId = reportId
    )
    val mockService: SendAReportService = mock[SendAReportService]

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[SendAReportService].toInstance(mockService)
        )
        .build()
      running(application) {
        when(mockService.reportingCPSOCompleteStatus(any(), any())).thenReturn(Completed)
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }

    "the Accounts task" - {
      val report   = ReportId(FATCA, 2025, None, "testFiID")
      val firstId  = AccountId("80")
      val secondId = AccountId("12")

      val noAccounts = emptyUserAnswers.withPage(ReportIdPage, report)

      val oneCompleteAccount = noAccounts
        .withPage(AccountIdPage(firstId)(report), firstId)
        .withPage(HaveNumberPage(firstId)(report), false)
        .withPage(IdentifierPage(firstId)(report), "FIRST-ACCOUNT")
        .withPage(AccountClosedPage(firstId)(report), false)
        .withPage(WhatWasTheAccountBalancePage(firstId)(report), AccountBalance(Currency.GBP, "100"))
        .withPage(HavePaymentsPage(firstId)(report), false)

      val mixedAccounts = oneCompleteAccount
        .withPage(AccountIdPage(secondId)(report), secondId)
        .withPage(HaveNumberPage(secondId)(report), false)
        .withPage(IdentifierPage(secondId)(report), "SECOND-ACCOUNT")

      val startAccountUrl = controllers.manual.account.routes.HaveNumberController.onPageLoad(NormalMode).url
      val overviewUrl     = controllers.manual.account.routes.AccountsController.onPageLoad().url

      Seq(
        ("there are no accounts", noAccounts, "sendAReport.status.notStarted", startAccountUrl),
        ("the only account is complete", oneCompleteAccount, "sendAReport.status.completed", overviewUrl),
        ("one of two accounts is incomplete", mixedAccounts, "sendAReport.status.incomplete", overviewUrl)
      ).foreach {
        case (scenario, answers, statusKey, expectedUrl) =>
          s"must show the correct status and link when $scenario" in {
            when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))
            val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
              val result  = route(application, request).value

              status(result) mustBe OK

              val page = Jsoup.parse(contentAsString(result))
              val msg  = messages(application)
              val accountsRow = page
                .select(".govuk-task-list__item")
                .asScala
                .find {
                  row =>
                    row.select(".govuk-task-list__link").text() == msg("sendAReport.accountsInformation.accounts")
                }
                .value

              accountsRow.select(".govuk-task-list__link").attr("href") mustBe expectedUrl
              accountsRow.select(".govuk-task-list__status").text() mustBe msg(statusKey)
            }
          }
      }
    }
  }
}
