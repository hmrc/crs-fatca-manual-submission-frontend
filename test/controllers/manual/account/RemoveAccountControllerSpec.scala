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
import forms.manual.account.RemoveAccountFormProvider
import models.SubmissionsConstants.FATCA
import models.viewModels.{AccountHolderId, AccountId}
import models.{ReportId, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import pages.ReportIdPage
import pages.manual.account.*
import pages.manual.accountHolders.AccountPage as HolderAccountsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.account.RemoveAccountView

import scala.concurrent.Future

class RemoveAccountControllerSpec extends SpecBase {

  private val selectedId = AccountId("42")
  private val identifier = "ACCOUNT-42"
  private val form       = new RemoveAccountFormProvider()()

  private lazy val removeUrl   = routes.RemoveAccountController.onPageLoad().url
  private lazy val accountsUrl = routes.AccountsController.onPageLoad().url

  private def selectedAccount(report: ReportId): UserAnswers =
    emptyUserAnswers
      .withPage(ReportIdPage, report)
      .withPage(CurrentAccountIdPage()(report), selectedId)
      .withPage(AccountIdPage(selectedId)(report), selectedId)
      .withPage(HaveNumberPage(selectedId)(report), false)
      .withPage(IdentifierPage(selectedId)(report), identifier)

  "RemoveAccountController" - {

    Seq(
      ("CRS", reportId, true),
      ("FATCA", reportId.copy(regime = FATCA), false)
    ).foreach {
      case (regimeName, report, isCrs) =>
        s"must display the selected identifier and the $regimeName confirmation view on GET" in {
          val answers = selectedAccount(report)
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))

          val application = applicationBuilder(Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, removeUrl)
            val result  = route(application, request).value
            val view    = application.injector.instanceOf[RemoveAccountView]

            status(result) mustBe OK
            contentAsString(result) mustBe
              view(form, identifier, isCrs)(request, messages(application)).toString
          }
        }
    }

    "must show the required error and skip saving when no answer is submitted" in {
      val connector = mock[DatabaseConnector]
      val application = applicationBuilder(Some(selectedAccount(reportId)))
        .overrides(bind[DatabaseConnector].toInstance(connector))
        .build()

      running(application) {
        val request        = FakeRequest(POST, removeUrl).withFormUrlEncodedBody()
        val result         = route(application, request).value
        val view           = application.injector.instanceOf[RemoveAccountView]
        val formWithErrors = form.bind(Map.empty[String, String])

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe
          view(formWithErrors, identifier, true)(request, messages(application)).toString
        contentAsString(result) must include(messages(application)("account.remove.account.error.required"))
        verify(connector, never()).set(any())(any())
      }
    }

    "must save the original answers and return to Accounts without a removal message when No is selected" in {
      val answers   = selectedAccount(reportId)
      val connector = mock[DatabaseConnector]
      when(connector.set(any())(any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(Some(answers))
        .overrides(bind[DatabaseConnector].toInstance(connector))
        .build()

      running(application) {
        val request = FakeRequest(POST, removeUrl).withFormUrlEncodedBody("value" -> "false")
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe accountsUrl
        flash(result).get("account-removed") mustBe None
        verify(connector).set(eqTo(answers))(any())
      }
    }

    "must remove the last account, clear its selection and flash its identifier when Yes is selected" in {
      val connector = mock[DatabaseConnector]
      when(connector.set(any())(any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(Some(selectedAccount(reportId)))
        .overrides(bind[DatabaseConnector].toInstance(connector))
        .build()

      running(application) {
        val request = FakeRequest(POST, removeUrl).withFormUrlEncodedBody("value" -> "true")
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe accountsUrl
        flash(result).get("account-removed") mustBe Some(identifier)

        val saved = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(connector).set(saved.capture())(any())

        saved.getValue.get(ReportIdPage) mustBe Some(reportId)
        saved.getValue.get(AccountDataPage(selectedId)) mustBe None
        saved.getValue.get(CurrentAccountIdPage()) mustBe None
      }
    }

    "must unlink the account from its holders while preserving other accounts, holders and reports" in {
      val keptId          = AccountId("80")
      val sharedHolder    = AccountHolderId("1")
      val soleHolder      = AccountHolderId("2")
      val unrelatedHolder = AccountHolderId("3")
      val otherReport     = reportId.copy(fiId = "another-fi")

      val sharedLinks    = HolderAccountsPage(reportId, sharedHolder)
      val soleLinks      = HolderAccountsPage(reportId, soleHolder)
      val unrelatedLinks = HolderAccountsPage(reportId, unrelatedHolder)
      val otherLinks     = HolderAccountsPage(otherReport, soleHolder)

      val answers = selectedAccount(reportId)
        .withPage(AccountIdPage(keptId)(reportId), keptId)
        .withPage(HaveNumberPage(keptId)(reportId), false)
        .withPage(IdentifierPage(keptId)(reportId), "KEEP")
        .withPage(sharedLinks, Set(selectedId, keptId))
        .withPage(soleLinks, Set(selectedId))
        .withPage(unrelatedLinks, Set(keptId))
        .withPage(AccountIdPage(selectedId)(otherReport), selectedId)
        .withPage(otherLinks, Set(selectedId))

      val connector = mock[DatabaseConnector]
      when(connector.set(any())(any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(Some(answers))
        .overrides(bind[DatabaseConnector].toInstance(connector))
        .build()

      running(application) {
        val request = FakeRequest(POST, removeUrl).withFormUrlEncodedBody("value" -> "true")
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe accountsUrl

        val saved = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(connector).set(saved.capture())(any())
        val updated = saved.getValue

        updated.get(AccountDataPage(selectedId)) mustBe None
        updated.get(CurrentAccountIdPage()) mustBe None
        updated.get(sharedLinks) mustBe Some(Set(keptId))
        updated.get(soleLinks) mustBe Some(Set.empty[AccountId])
        updated.get(unrelatedLinks) mustBe Some(Set(keptId))
        updated.get(AccountDataPage(keptId)) mustBe answers.get(AccountDataPage(keptId))
        (updated.data \ otherReport.mongoKey) mustBe (answers.data \ otherReport.mongoKey)
      }
    }

    "must redirect to journey recovery on GET if the selected account no longer exists" in {
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountIdPage(), selectedId)

      val application = applicationBuilder(Some(answers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, removeUrl)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery without saving on POST if the account exists only in another report" in {
      val otherReport = reportId.copy(fiId = "another-fi")
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountIdPage()(reportId), selectedId)
        .withPage(AccountIdPage(selectedId)(otherReport), selectedId)
        .withPage(HaveNumberPage(selectedId)(otherReport), false)
        .withPage(IdentifierPage(selectedId)(otherReport), identifier)

      val connector = mock[DatabaseConnector]
      val application = applicationBuilder(Some(answers))
        .overrides(bind[DatabaseConnector].toInstance(connector))
        .build()

      running(application) {
        val request = FakeRequest(POST, removeUrl).withFormUrlEncodedBody("value" -> "true")
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
        flash(result).get("account-removed") mustBe None
        verify(connector, never()).set(any())(any())
      }
    }
  }
}
