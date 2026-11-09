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

package controllers.manual.account

import base.SpecBase
import connectors.DatabaseConnector
import models.{AccountBalance, Currency, NormalMode, UserAnswers}
import models.SubmissionsConstants.FATCA
import models.viewModels.AccountId
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import pages.ReportIdPage
import pages.manual.account.*
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ViewFIService

import java.time.Instant
import scala.concurrent.Future

class AccountsControllerSpec extends SpecBase {

  private val report   = reportId.copy(regime = FATCA)
  private val firstId  = AccountId("80")
  private val secondId = AccountId("12")

  private val accountsUrl = routes.AccountsController.onPageLoad().url
  private val addUrl      = routes.AccountsController.onAddAccount().url
  private val reportUrl   = controllers.manual.routes.SendAReportController.onPageLoad().url

  private val reportAnswers = emptyUserAnswers.withPage(ReportIdPage, report)

  private val oneComplete = reportAnswers
    .withPage(AccountIdPage(firstId)(report), firstId)
    .withPage(
      AccountCreationPage(firstId)(report),
      Instant.parse("2026-11-09T10:00:00Z")
    )
    .withPage(HaveNumberPage(firstId)(report), false)
    .withPage(IdentifierPage(firstId)(report), "FIRST-ACCOUNT")
    .withPage(AccountClosedPage(firstId)(report), false)
    .withPage(WhatWasTheAccountBalancePage(firstId)(report), AccountBalance(Currency.GBP, "100"))
    .withPage(HavePaymentsPage(firstId)(report), false)

  private val withUnlabelledAccount = oneComplete
    .withPage(AccountIdPage(secondId)(report), secondId)
    .withPage(
      AccountCreationPage(secondId)(report),
      Instant.parse("2026-11-09T11:00:00Z")
    )
    .withPage(HaveNumberPage(secondId)(report), false)

  private val mixedAccounts = withUnlabelledAccount
    .withPage(IdentifierPage(secondId)(report), "SECOND-ACCOUNT")

  private def withApp(answers: UserAnswers)(
    test: (Application, DatabaseConnector) => Unit
  ): Unit = {
    val connector = mock[DatabaseConnector]
    val fiService = mock[ViewFIService]

    when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))
    when(connector.set(any())(any())).thenReturn(Future.successful(()))
    when(fiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

    val application = applicationBuilder(Some(answers))
      .overrides(
        bind[DatabaseConnector].toInstance(connector),
        bind[ViewFIService].toInstance(fiService)
      )
      .build()

    running(application) {
      test(application, connector)
    }
  }

  "AccountsController" - {

    "must show the empty overview with links to add an account and return to the report" in {
      withApp(reportAnswers) {
        (app, _) =>
          val result = route(app, FakeRequest(GET, accountsUrl)).value

          status(result) mustBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.select("h1").text() mustBe "You have not added any accounts"
          page.select(s"a[href='$addUrl']").text() mustBe "Add an account"
          page.select(s"a[href='$reportUrl']").text() must include(fiDetail.FIName)
          page.select("input[type=radio]").size() mustBe 0
      }
    }

    "must count only the visible account when another has no saved number or identifier" in {
      withApp(withUnlabelledAccount) {
        (app, _) =>
          val result = route(app, FakeRequest(GET, accountsUrl)).value

          status(result) mustBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.select("h1").text() mustBe "You have added 1 account"
          val rows = page.select(".govuk-summary-list__row")
          rows.size() mustBe 1
          rows.first().select(".govuk-summary-list__key").text() mustBe "FIRST-ACCOUNT"
      }
    }

    "must list accounts in creation-time order and tag only the incomplete account" in {
      withApp(mixedAccounts) {
        (app, _) =>
          val result = route(app, FakeRequest(GET, accountsUrl)).value

          status(result) mustBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.select("h1").text() mustBe "You have added 2 accounts"

          val rows = page.select(".govuk-summary-list__row")
          rows.size() mustBe 2
          rows.get(0).select(".govuk-summary-list__key").text() mustBe "FIRST-ACCOUNT"
          rows.get(0).select(".govuk-tag").size() mustBe 0
          rows.get(1).select(".govuk-summary-list__key").text() must include("SECOND-ACCOUNT")
          rows.get(1).select(".govuk-tag").text() mustBe "Incomplete"
      }
    }

    "must show the required error and skip saving when Continue is pressed without an answer" in {
      withApp(oneComplete) {
        (app, connector) =>
          val request = FakeRequest(POST, accountsUrl).withFormUrlEncodedBody()
          val result  = route(app, request).value

          status(result) mustBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.select(".govuk-error-summary").text() must include(
            "Select yes if you need to add another account"
          )
          verify(connector, never()).set(any())(any())
      }
    }

    "must send Yes to the add-account route" in {
      withApp(oneComplete) {
        (app, connector) =>
          val request = FakeRequest(POST, accountsUrl).withFormUrlEncodedBody("value" -> "true")
          val result  = route(app, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe addUrl
          val expected = oneComplete.withPage(AddAnotherAccountPage(report), true)
          verify(connector).set(eqTo(expected))(any())
      }
    }

    "must clear the previous selection before starting another account" in {
      val answers = oneComplete
        .withPage(CurrentAccountIdPage()(report), firstId)
        .withPage(AddAnotherAccountPage(report), true)

      withApp(answers) {
        (app, connector) =>
          val result = route(app, FakeRequest(GET, addUrl)).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.HaveNumberController.onPageLoad(NormalMode).url
          verify(connector).set(eqTo(oneComplete))(any())
      }
    }

    Seq(
      ("one account is incomplete", mixedAccounts, reportUrl),
      ("all accounts are complete", oneComplete, controllers.routes.UnderConstructionController.onPageLoad().url)
    ).foreach {
      case (scenario, answers, destination) =>
        s"must save No and redirect correctly when $scenario" in {
          withApp(answers) {
            (app, connector) =>
              val request = FakeRequest(POST, accountsUrl).withFormUrlEncodedBody("value" -> "false")
              val result  = route(app, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe destination
              val expected = answers.withPage(AddAnotherAccountPage(report), false)
              verify(connector).set(eqTo(expected))(any())
          }
        }
    }

    Seq(
      ("Change", routes.AccountsController.onChange(secondId.value), routes.AccountCheckAnswersController.onPageLoad()),
      ("Remove", routes.AccountsController.onRemove(secondId.value), routes.RemoveAccountController.onPageLoad())
    ).foreach {
      case (link, entry, destination) =>
        s"must select the second account when its $link link is followed" in {
          val answers = mixedAccounts.withPage(CurrentAccountIdPage()(report), firstId)

          withApp(answers) {
            (app, connector) =>
              val result = route(app, FakeRequest(GET, entry.url)).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe destination.url
              val expected = answers.withPage(CurrentAccountIdPage()(report), secondId)
              verify(connector).set(eqTo(expected))(any())
          }
        }
    }

    "must redirect to journey recovery without saving when the requested account ID is unknown" in {
      withApp(oneComplete) {
        (app, connector) =>
          val url    = routes.AccountsController.onRemove("missing").url
          val result = route(app, FakeRequest(GET, url)).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
          verify(connector, never()).set(any())(any())
      }
    }

    "must show the removal banner on the empty overview when the request carries removal flash" in {
      withApp(reportAnswers) {
        (app, _) =>
          val afterRemoval = FakeRequest(GET, accountsUrl)
            .withFlash("account-removed" -> "REMOVED-42")

          val result = route(app, afterRemoval).value
          status(result) mustBe OK

          val page   = Jsoup.parse(contentAsString(result))
          val banner = page.select(".govuk-notification-banner--success")

          page.select("h1").text() mustBe "You have not added any accounts"
          banner.size() mustBe 1
          banner.attr("role") mustBe "alert"
          banner.select(".govuk-notification-banner__heading").text() mustBe
            "Account REMOVED-42 removed."

          val laterVisit = route(app, FakeRequest(GET, accountsUrl)).value
          status(laterVisit) mustBe OK
          Jsoup
            .parse(contentAsString(laterVisit))
            .select(".govuk-notification-banner--success")
            .size() mustBe 0
      }
    }

    "must give each Change and Remove link the correct destination and account-specific accessible text" in {
      withApp(mixedAccounts) {
        (app, _) =>
          val result = route(app, FakeRequest(GET, accountsUrl)).value
          status(result) mustBe OK

          val rows = Jsoup.parse(contentAsString(result)).select(".govuk-summary-list__row")
          rows.size() mustBe 2

          val expectedAccounts = Seq(firstId -> "FIRST-ACCOUNT", secondId -> "SECOND-ACCOUNT")

          expectedAccounts.zipWithIndex.foreach {
            case ((id, identifier), index) =>
              val links = rows.get(index).select(".govuk-summary-list__actions a")
              links.size() mustBe 2

              val change = links.get(0)
              change.attr("href") mustBe routes.AccountsController.onChange(id.value).url
              change.select("span[aria-hidden=true]").text() mustBe "Change"
              change.select(".govuk-visually-hidden").text() mustBe s"Change account $identifier"

              val remove = links.get(1)
              remove.attr("href") mustBe routes.AccountsController.onRemove(id.value).url
              remove.select("span[aria-hidden=true]").text() mustBe "Remove"
              remove.select(".govuk-visually-hidden").text() mustBe s"Remove account $identifier"
          }
      }
    }
  }
}
