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
import forms.manual.accountHolders.AccountHolderListFormProvider
import java.time.Instant
import models.ServiceErrors.NoFiDetailFound
import models.manual.accountHolders.AccountHolderTypes
import models.manual.accountHolders.AccountHolderTypes.*
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.response.FIDetail
import models.viewModels.{Account, AccountHolder, AccountHolderId, AccountHolders, AccountId, Accounts}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.{AccountHoldersPage, AccountPage, DoYouWantToAddAnotherAccountHolder}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ViewFIService
import views.html.manual.accountHolders.AccountHolderListView

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class AccountHolderListControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute: Call = Call("GET", "/foo")

  private val linkedAccount = Account(
    haveNumber = Some(true),
    whatIsAccountNumber = Some("GB33BUKB20201555555555"),
    accountId = AccountId("80")
  )

  private val unlinkedAccount = Account(
    haveNumber = Some(true),
    whatIsAccountNumber = Some("GB11AAAA11111111111111"),
    accountId = AccountId("82")
  )

  private val namedHolder = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Alpha Holdings"),
    accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson)
  )

  private val unnamedHolder = AccountHolder(
    individualOrOrganisation = Some(Individual)
  )

  private val oldestCreated = Instant.parse("2025-03-01T09:00:00Z")
  private val middleCreated = Instant.parse("2025-08-15T12:30:00Z")
  private val newestCreated = Instant.parse("2026-01-20T16:45:00Z")

  private def organisationHolder(name: String, creationDateTime: Option[Instant]): AccountHolder =
    AccountHolder(
      individualOrOrganisation = Some(Organisation),
      organisationName = Some(name),
      accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson),
      creationDateTime = creationDateTime
    )

  private val oldestHolder  = organisationHolder("Oldest Holdings", Some(oldestCreated))
  private val middleHolder  = organisationHolder("Middle Holdings", Some(middleCreated))
  private val newestHolder  = organisationHolder("Newest Holdings", Some(newestCreated))
  private val undatedHolder = organisationHolder("Undated Holdings", None)

  private val formProvider = new AccountHolderListFormProvider()
  private val form         = formProvider()

  private lazy val accountHolderListRoute =
    controllers.manual.accountHolders.routes.AccountHolderListController.onPageLoad(NormalMode).url

  private lazy val changeAccountHolderListRoute =
    controllers.manual.accountHolders.routes.AccountHolderListController.onPageLoad(CheckMode).url

  private def userAnswersFor(
    holders: Map[String, AccountHolder] = Map("21" -> namedHolder),
    links: Map[String, Set[AccountId]] = Map("21" -> Set(linkedAccount.accountId))
  ): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(
        AccountsPage(reportId),
        Accounts(
          currentAccountId = Some(linkedAccount.accountId),
          accounts = Map(
            linkedAccount.accountId.value   -> linkedAccount,
            unlinkedAccount.accountId.value -> unlinkedAccount
          )
        )
      )
      .withPage(AccountHoldersPage(), AccountHolders(None, holders))

    links.foldLeft(answers) {
      case (updated, (accountHolderId, accountIds)) =>
        updated.withPage(AccountPage(reportId, AccountHolderId(accountHolderId)), accountIds)
    }
  }

  private def mockFiService(result: Future[FIDetail] = Future.successful(fiDetail)): ViewFIService = {
    val service = mock[ViewFIService]
    when(service.getFIDetail(any(), any())(using any())) thenReturn result
    service
  }

  private def expectedView(
    app: Application,
    req: FakeRequest[?],
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    holders: Seq[(AccountHolderId, AccountHolder)] = Seq(AccountHolderId("21") -> namedHolder)
  ): String =
    app.injector
      .instanceOf[AccountHolderListView]
      .apply(
        boundForm,
        mode,
        fiDetail.FIName,
        holders,
        Seq("GB11AAAA11111111111111"),
        reportId.regime
      )(req, messages(app), req.flash)
      .toString

  private def renderedAccountHolderNames(result: Future[play.api.mvc.Result]): List[String] =
    Jsoup
      .parse(contentAsString(result))
      .select(".govuk-summary-list__row dt span")
      .eachText()
      .asScala
      .map(_.trim)
      .toList

  "AccountHolderList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request)
      }
    }

    "must order account holders by creationDateTime, oldest first" in {

      val holders = Map(
        "21" -> newestHolder,
        "22" -> oldestHolder,
        "23" -> middleHolder
      )

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor(holders = holders, links = Map.empty)))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        renderedAccountHolderNames(result) mustBe List(
          "Oldest Holdings",
          "Middle Holdings",
          "Newest Holdings"
        )
      }
    }

    "must list account holders without a creationDateTime before those that have one" in {

      val holders = Map(
        "21" -> newestHolder,
        "22" -> undatedHolder,
        "23" -> oldestHolder
      )

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor(holders = holders, links = Map.empty)))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        renderedAccountHolderNames(result) mustBe List(
          "Undated Holdings",
          "Oldest Holdings",
          "Newest Holdings"
        )
      }
    }

    "must return OK and the correct view for a GET in check mode" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, changeAccountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request, mode = CheckMode)
      }
    }

    "must render the success banner when an account holder has been removed" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute).withFlash("account-holder-removed" -> "Alpha Holdings")

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request)
        contentAsString(result) must include("govuk-notification-banner--success")
        contentAsString(result) must include("Account holder Alpha Holdings removed.")
      }
    }

    "must not offer account holders without a name to the view" in {

      val answers = userAnswersFor(
        holders = Map("22" -> unnamedHolder),
        links = Map("22" -> Set(linkedAccount.accountId))
      )

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView(application, request, holders = Seq.empty)
      }
    }

    "must redirect to Journey Recovery for a GET when the financial institution cannot be found" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService(Future.failed(NoFiDetailFound))))
          .build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(
            bind[ViewFIService].toInstance(mockFiService()),
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderListRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(DoYouWantToAddAnotherAccountHolder()) mustBe Some(true)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService()))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderListRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual expectedView(application, request, boundForm = boundForm)
      }
    }

    "must redirect to Journey Recovery for a POST when the financial institution cannot be found" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersFor()))
          .overrides(bind[ViewFIService].toInstance(mockFiService(Future.failed(NoFiDetailFound))))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderListRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, accountHolderListRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, accountHolderListRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
