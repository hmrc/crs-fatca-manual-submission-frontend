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
import models.SubmissionsConstants.{CRS, FATCA}
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import connectors.DatabaseConnector
import forms.manual.accountHolders.AccountFormProvider
import models.viewModels.{Account, AccountHolderId, AccountId, Accounts}
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.{AccountPage, CurrentAccountHolderIdPage}
import services.ViewFIService
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import views.html.manual.accountHolders.AccountView
import viewmodels.govuk.all.CheckboxItemViewModel
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class AccountControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val accountRoute            = controllers.manual.accountHolders.routes.AccountController.onPageLoad(NormalMode).url
  private val reportId             = ReportId(CRS, 2025, None, "testFiID")
  private val accountHolderId      = AccountHolderId("testId")
  val mockFiService: ViewFIService = mock[ViewFIService]
  private val accId                = AccountId("1")

  private val accounts = Accounts(currentAccountId = None,
                                  accounts = Map(
                                    "1" -> Account(
                                      haveNumber = Some(false),
                                      identifier = Some("testId"),
                                      numberType = None,
                                      accountId = accId
                                    )
                                  )
  )

  val ua = emptyUserAnswers
    .withPage(ReportIdPage, reportId)
    .withPage(CurrentAccountHolderIdPage()(reportId), accountHolderId)
    .withPage(AccountsPage(reportId), accounts)

  val existingIdAndNumbers: Seq[(AccountId, String)] =
    accounts.accounts.values.flatMap {
      acc => acc.accountNumber.map(acc.accountId -> _)
    }.toSeq
  val existingIds: Set[AccountId] = existingIdAndNumbers.map(_._1).toSet

  val options: Seq[CheckboxItem] =
    existingIdAndNumbers.zipWithIndex.map {
      case ((accountId, accountNumber), index) =>
        CheckboxItemViewModel(
          content = Text(accountNumber),
          fieldId = "value",
          index = index,
          value = accountId.value
        )
          .copy(name = Some(s"value[$index]"))
    }
  val formProvider = new AccountFormProvider()
  val form         = formProvider(existingIds.toSeq)

  "Account Controller" - {

    "must return OK and the correct view for a GET - CRS" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()
      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, accountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, fiDetail.FIName, options, true)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET with options when the question has previously been answered" in {

      val userAnswers = ua.set(AccountPage(reportId, accountHolderId), Set(accId)).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, accountRoute)

        val view = application.injector.instanceOf[AccountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(existingIds), NormalMode, fiDetail.FIName, options, true)(request, messages(application)).toString
      }
    }

    "must redirect when FIDetail fails to retrieve" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()
      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.failed(InternalServerException("Failed")))

      running(application) {
        val request = FakeRequest(GET, accountRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET with no options when the question has previously been answered - CRS" in {

      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage()(reportId), accountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, accountRoute)

        val view = application.injector.instanceOf[AccountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(existingIds), NormalMode, fiDetail.FIName, Seq.empty, true)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET with no options when the question has previously been answered - FATCA" in {

      val reportId = ReportId(FATCA, 2025, None, "testFiID")

      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage()(reportId), accountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, accountRoute)

        val view = application.injector.instanceOf[AccountView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(existingIds), NormalMode, fiDetail.FIName, Seq.empty, false)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[ViewFIService].toInstance(mockFiService)
          )
          .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, accountRoute)
            .withFormUrlEncodedBody(("value[0]", "1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, accountRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AccountView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiDetail.FIName, options, true)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, accountRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, accountRoute)
            .withFormUrlEncodedBody(("value[0]", "1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
