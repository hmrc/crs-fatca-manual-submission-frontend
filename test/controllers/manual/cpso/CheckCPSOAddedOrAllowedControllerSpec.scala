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
import controllers.routes
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.manual.cpso.IndividualName
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner, ControllingOrSubstantialOwners}
import models.viewModels.{AccountHolder, AccountHolders}
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.cpso.ControllingOrSubstantialOwnersPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class CheckCPSOAddedOrAllowedControllerSpec extends SpecBase with MockitoSugar {

  private val cpsoId = CPSOId("50")

  private val accountHolderRequiringCpso = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Alpha Holdings"),
    accountHolderType = Some("CRS101")
  )

  private val accountHolderWithoutName = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    accountHolderType = Some("CRS101")
  )

  private val individualAccountHolder = AccountHolder(
    individualOrOrganisation = Some(Individual),
    organisationName = Some("Beta Holdings")
  )

  private val organisationThatCannotHaveCpso = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Gamma Holdings"),
    accountHolderType = Some("CRS102")
  )

  private val cpso = ControllingOrSubstantialOwner(
    accountHolders = None,
    individualName = Some(IndividualName("Some", "Name")),
    organisationName = None
  )

  private lazy val checkCPSORoute =
    controllers.manual.cpso.routes.CheckCPSOAddedOrAllowedController.onChangeRedirect().url

  private def userAnswersFor(
    cpsos: Map[String, ControllingOrSubstantialOwner] = Map.empty,
    currentCpsoId: Option[CPSOId] = None,
    accountHolders: Map[String, AccountHolder] = Map.empty
  ): UserAnswers =
    emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(ControllingOrSubstantialOwnersPage(), ControllingOrSubstantialOwners(currentCpsoId, cpsos))
      .withPage(AccountHoldersPage(), AccountHolders(None, accountHolders))

  "CheckCPSOAddedOrAllowed Controller" - {

    "must redirect to the controlling persons page when a CP-SO has already been added" in {

      val answers = userAnswersFor(
        cpsos = Map(cpsoId.value -> cpso),
        currentCpsoId = Some(cpsoId),
        accountHolders = Map("53" -> accountHolderRequiringCpso)
      )

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnderConstructionController.onPageLoad().url
      }
    }

    "must redirect to the account holder page when no CP-SO has been added but an account holder requires one" in {

      val answers = userAnswersFor(accountHolders = Map("53" -> accountHolderRequiringCpso))

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.manual.cpso.routes.AccountHolderController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the account holder page in check mode when the action is invoked in check mode" in {

      val answers = userAnswersFor(accountHolders = Map("53" -> accountHolderRequiringCpso))

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val controller = application.injector.instanceOf[CheckCPSOAddedOrAllowedController]

        val result = controller.onChangeRedirect(CheckMode)(FakeRequest())

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.manual.cpso.routes.AccountHolderController.onPageLoad(CheckMode).url
      }
    }

    "must redirect to Journey Recovery when no account holder can have a CP-SO" in {

      val answers = userAnswersFor(
        accountHolders = Map(
          "53" -> individualAccountHolder,
          "54" -> organisationThatCannotHaveCpso
        )
      )

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when the account holder that can have a CP-SO has no name" in {

      val answers = userAnswersFor(accountHolders = Map("53" -> accountHolderWithoutName))

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when there are no account holders" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor())).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must not treat a current CP-SO id without a CP-SO entry as a CP-SO having been added" in {

      val answers = userAnswersFor(
        currentCpsoId = Some(cpsoId),
        accountHolders = Map("53" -> accountHolderRequiringCpso)
      )

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.manual.cpso.routes.AccountHolderController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkCPSORoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
