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

package controllers.manual.accountHolders

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.accountHolders.RemoveAccountHolderFormProvider
import models.manual.accountHolders.IndividualOrOrganisation.Organisation
import models.manual.cpso.IndividualName
import models.viewModels.AccountHolderId
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner, ControllingOrSubstantialOwners}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderOrganisationNamePage, AccountHolderPage, CurrentAccountHolderIdPage, IndividualOrOrganisationPage}
import pages.manual.cpso.{AccountHolderIdsPage, CPSOPage, ControllingOrSubstantialOwnersPage, CurrentCPSOIdPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.RemoveAccountHolderView

import scala.concurrent.Future

class RemoveAccountHolderControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute: Call = Call("GET", "/foo")

  private val accountHolderId      = AccountHolderId("21")
  private val otherAccountHolderId = AccountHolderId("37")
  private val accountHolderName    = "Alpha Holdings"

  private val linkedCpsoId   = CPSOId("80")
  private val unlinkedCpsoId = CPSOId("81")
  private val sharedCpsoId   = CPSOId("82")

  private val formProvider        = new RemoveAccountHolderFormProvider()
  private val form: Form[Boolean] = formProvider()

  private lazy val removeAccountHolderRoute =
    controllers.manual.accountHolders.routes.RemoveAccountHolderController.onPageLoad(NormalMode).url

  private lazy val changeRemoveAccountHolderRoute =
    controllers.manual.accountHolders.routes.RemoveAccountHolderController.onPageLoad(CheckMode).url

  private def ownersExclusiveToTheAccountHolder: ControllingOrSubstantialOwners =
    ControllingOrSubstantialOwners(
      currentCpsoId = Some(linkedCpsoId),
      cpsos = Map(
        linkedCpsoId.value ->
          ControllingOrSubstantialOwner(Some(Seq(accountHolderId.value)), Some(IndividualName("Thando", "Mafela")), None),
        unlinkedCpsoId.value ->
          ControllingOrSubstantialOwner(Some(Seq("99")), Some(IndividualName("Another", "Owner")), None)
      )
    )

  /** A CP-SO shared by the account holder being removed and a second one, `37`. */
  private def ownersSharedWithAnotherAccountHolder: ControllingOrSubstantialOwners =
    ControllingOrSubstantialOwners(
      currentCpsoId = Some(sharedCpsoId),
      cpsos = Map(
        sharedCpsoId.value -> ControllingOrSubstantialOwner(
          Some(Seq(accountHolderId.value, otherAccountHolderId.value)),
          Some(IndividualName("Thando", "Mafela")),
          None
        )
      )
    )

  private def userAnswersFor(
                              withName: Boolean = true,
                              owners: ControllingOrSubstantialOwners = ownersExclusiveToTheAccountHolder
                            ): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountHolderIdPage(), accountHolderId)
      .withPage(IndividualOrOrganisationPage(accountHolderId), Organisation)
      .withPage(ControllingOrSubstantialOwnersPage(), owners)

    if (withName) answers.withPage(AccountHolderOrganisationNamePage(accountHolderId), accountHolderName) else answers
  }

  private def expectedView(
                            app: Application,
                            req: FakeRequest[?],
                            boundForm: Form[_] = form,
                            mode: Mode = NormalMode
                          ): String =
    app.injector
      .instanceOf[RemoveAccountHolderView]
      .apply(boundForm, mode, reportId.regime.value, accountHolderName)(req, messages(app))
      .toString

  "RemoveAccountHolder Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor())).build()

      running(application) {
        val request = FakeRequest(GET, removeAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request)
      }
    }

    "must return OK and the correct view for a GET in check mode" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor())).build()

      running(application) {
        val request = FakeRequest(GET, changeRemoveAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request, mode = CheckMode)
      }
    }

    "must remove the account holder, save the answers, flash the removed name and redirect when the user says yes" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        flash(result).get("account-holder-removed") mustBe Some(accountHolderName)

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(AccountHolderPage(accountHolderId, reportId)) mustBe None
      }
    }

    "must keep the account holder, save the answers and redirect without a flash message when the user says no" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        flash(result).get("account-holder-removed") mustBe None

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(AccountHolderPage(accountHolderId, reportId)) must not be empty
      }
    }

    "must also remove the CP-SO linked to the account holder and clear the current CP-SO id" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(CPSOPage(linkedCpsoId)) mustBe None
        captor.getValue.get(CPSOPage(unlinkedCpsoId)) must not be empty
        captor.getValue.get(CurrentCPSOIdPage()) mustBe None
      }
    }

    "must only unlink the account holder from a CP-SO that another account holder is still linked to" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor(owners = ownersSharedWithAnotherAccountHolder)))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        val saved = captor.getValue

        saved.get(CPSOPage(sharedCpsoId)) must not be empty
        saved.get(AccountHolderIdsPage(sharedCpsoId, reportId)) mustBe Some(Set(otherAccountHolderId))
        saved.get(CurrentCPSOIdPage()) mustBe Some(sharedCpsoId)
      }
    }

    "must keep the linked CP-SO when the user says no" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(CPSOPage(linkedCpsoId)) must not be empty
        captor.getValue.get(CurrentCPSOIdPage()) mustBe Some(linkedCpsoId)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor())).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual expectedView(application, request, boundForm = boundForm)
      }
    }

    "must redirect to Journey Recovery for a GET when no account holder has been selected" in {

      val answers = emptyUserAnswers.withPage(ReportIdPage, reportId)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, removeAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when the account holder has no name" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(withName = false))).build()

      running(application) {
        val request = FakeRequest(GET, removeAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when the account holder has no name" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(withName = false))).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
