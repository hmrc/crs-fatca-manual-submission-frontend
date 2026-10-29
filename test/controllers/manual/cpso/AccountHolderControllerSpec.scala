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

package controllers.manual.cpso

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.AccountHolderFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.response.FIDetail
import models.viewModels.manual.cpso.CPSOId
import models.viewModels.{AccountHolder, AccountHolderId, AccountHolders}
import models.{Mode, NormalMode, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.cpso.{AccountHolderPage, CurrentCPSOIdPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ViewFIService
import views.html.manual.cpso.AccountHolderView

import scala.concurrent.Future

class AccountHolderControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute: Call = Call("GET", "/foo")

  private val cpsoId = CPSOId("cpso-id")

  private val eligibleHolderId   = AccountHolderId("1")
  private val individualHolderId = AccountHolderId("2")
  private val ineligibleHolderId = AccountHolderId("3")

  private val eligibleHolder = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Alpha Ltd"),
    accountHolderType = Some("CRS101")
  )

  private val individualHolder = AccountHolder(
    individualOrOrganisation = Some(Individual),
    organisationName = Some("Beta Ltd")
  )

  private val ineligibleHolder = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Gamma Ltd"),
    accountHolderType = Some("CRS102")
  )

  private val allAccountHolders = AccountHolders(
    currentAccountHolderId = None,
    accountHolders = Map(
      eligibleHolderId.value   -> eligibleHolder,
      individualHolderId.value -> individualHolder,
      ineligibleHolderId.value -> ineligibleHolder
    )
  )

  private val onlyEligibleAccountHolder = AccountHolders(
    currentAccountHolderId = None,
    accountHolders = Map(eligibleHolderId.value -> eligibleHolder)
  )

  private val formProvider = new AccountHolderFormProvider()

  private lazy val accountHolderRoute =
    controllers.manual.cpso.routes.AccountHolderController.onPageLoad(NormalMode).url

  private def userAnswersFor(
    accountHolders: AccountHolders = allAccountHolders,
    selected: Option[Set[AccountHolderId]] = None
  ): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)
      .withPage(AccountHoldersPage(), accountHolders)

    selected.fold(answers)(
      ids => answers.withPage(AccountHolderPage(cpsoId, reportId), ids)
    )
  }

  private def mockFiService(result: Future[FIDetail] = Future.successful(fiDetail)): ViewFIService = {
    val service = mock[ViewFIService]
    when(service.getFIDetail(any(), any())(using any())) thenReturn result
    service
  }

  private def expectedView(
    app: play.api.Application,
    req: FakeRequest[?],
    boundForm: play.api.data.Form[_] = formProvider(Seq(eligibleHolderId)),
    mode: Mode = NormalMode
  ): String =
    app.injector
      .instanceOf[AccountHolderView]
      .apply(
        boundForm,
        mode,
        fiDetail.FIName,
        AccountHolders.checkboxOptions(Seq(eligibleHolderId -> eligibleHolder)),
        reportId.regime.value
      )(req, messages(app))
      .toString

  "AccountHolder Controller" - {

    "must return OK and the correct view for a GET" in {

      val answers = userAnswersFor(accountHolders = onlyEligibleAccountHolder)

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request)
      }
    }

    "must only offer account holders that can have a CP-SO" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val answers = userAnswersFor(
        accountHolders = onlyEligibleAccountHolder,
        selected = Some(Set(eligibleHolderId))
      )

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          expectedView(application, request, formProvider(Seq(eligibleHolderId)).fill(Set(eligibleHolderId)))
      }
    }

    "must discard a previously selected account holder that can no longer have a CP-SO" in {

      val answers = userAnswersFor(
        accountHolders = onlyEligibleAccountHolder,
        selected = Some(Set(eligibleHolderId, individualHolderId))
      )

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          expectedView(application, request, formProvider(Seq(eligibleHolderId)).fill(Set(eligibleHolderId)))
      }
    }

    "must redirect to Journey Recovery for a GET when the financial institution cannot be found" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService(Future.failed(NoFiDetailFound))))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must save the selected account holders and redirect to the next page when valid data is submitted" in {

      val answers = userAnswersFor(accountHolders = onlyEligibleAccountHolder)

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers))
          .overrides(
            bind[ViewFIService].toInstance(mockFiService()),
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderRoute)
            .withFormUrlEncodedBody(("value[0]", eligibleHolderId.value))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(AccountHolderPage(cpsoId, reportId)) mustBe Some(Set(eligibleHolderId))
      }
    }

    "must redirect to Journey Recovery for a POST when the financial institution cannot be found" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService(Future.failed(NoFiDetailFound))))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderRoute)
            .withFormUrlEncodedBody(("value[0]", eligibleHolderId.value))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when nothing is selected" in {

      val answers = userAnswersFor(accountHolders = onlyEligibleAccountHolder)

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(POST, accountHolderRoute)

        val boundForm = formProvider(Seq(eligibleHolderId)).bind(Map.empty[String, String])

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual expectedView(application, request, boundForm)
      }
    }

    "must return a Bad Request when an account holder that cannot have a CP-SO is submitted" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderRoute)
            .withFormUrlEncodedBody(("value[0]", individualHolderId.value))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, accountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderRoute)
            .withFormUrlEncodedBody(("value[0]", eligibleHolderId.value))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
