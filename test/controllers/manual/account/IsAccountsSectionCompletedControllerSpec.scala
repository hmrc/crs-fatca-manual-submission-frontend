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
import models.{AccountBalance, Currency, UserAnswers}
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

import scala.concurrent.Future

class IsAccountsSectionCompletedControllerSpec extends SpecBase {

  private val report   = reportId.copy(regime = FATCA)
  private val firstId  = AccountId("80")
  private val secondId = AccountId("12")

  private val reportUrl = controllers.manual.routes.SendAReportController.onPageLoad().url

  private val reportAnswers = emptyUserAnswers.withPage(ReportIdPage, report)

  private val oneComplete = reportAnswers
    .withPage(AccountIdPage(firstId)(report), firstId)
    .withPage(HaveNumberPage(firstId)(report), false)
    .withPage(IdentifierPage(firstId)(report), "FIRST-ACCOUNT")
    .withPage(AccountClosedPage(firstId)(report), false)
    .withPage(WhatWasTheAccountBalancePage(firstId)(report), AccountBalance(Currency.GBP, "100"))
    .withPage(HavePaymentsPage(firstId)(report), false)

  private val withUnlabelledAccount = oneComplete
    .withPage(AccountIdPage(secondId)(report), secondId)
    .withPage(HaveNumberPage(secondId)(report), false)

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

  "IsAccountsSectionCompletedController" - {
    val url      = routes.IsAccountsSectionCompletedController.onPageLoad().url
    val question = IsAccountsSectionCompletedPage(report)

    "must display the selected FI and prefill the saved answer" in {
      withApp(oneComplete.withPage(question, false)) {
        (app, _) =>
          val result = route(app, FakeRequest(GET, url)).value

          status(result) mustBe OK

          val page = Jsoup.parse(contentAsString(result))
          page.select("h1").text() mustBe s"Have you finished adding the accounts for ${fiDetail.FIName}?"
          page.select("input[checked]").attr("value") mustBe "false"
      }
    }

    "must show the required error without saving an empty answer" in {
      withApp(oneComplete) {
        (app, connector) =>
          val request = FakeRequest(POST, url).withFormUrlEncodedBody("value" -> "")
          val result  = route(app, request).value

          status(result) mustBe BAD_REQUEST

          val page = Jsoup.parse(contentAsString(result))
          page.select(".govuk-error-summary a").text() mustBe
            "Select yes if you have finished adding the accounts for the financial institution"

          verify(connector, never()).set(any())(any())
      }
    }

    Seq(true, false).foreach {
      answer =>
        s"must save $answer and return to the task list" in {
          withApp(oneComplete) {
            (app, connector) =>
              val request = FakeRequest(POST, url).withFormUrlEncodedBody("value" -> answer.toString)
              val result  = route(app, request).value

              redirectLocation(result) mustBe Some(reportUrl)
              verify(connector).set(eqTo(oneComplete.withPage(question, answer)))(any())
          }
        }
    }

    Seq(
      "no accounts"                      -> reportAnswers,
      "an incomplete unlabelled account" -> withUnlabelledAccount
    ).foreach {
      case (scenario, answers) =>
        s"must return to the task list on GET and POST with $scenario" in {
          withApp(answers) {
            (app, connector) =>
              val getResult = route(app, FakeRequest(GET, url)).value
              redirectLocation(getResult) mustBe Some(reportUrl)

              val post       = FakeRequest(POST, url).withFormUrlEncodedBody("value" -> "true")
              val postResult = route(app, post).value
              redirectLocation(postResult) mustBe Some(reportUrl)

              verify(connector, never()).set(any())(any())
          }
        }
    }
  }
}
