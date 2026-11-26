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

import connectors.{DatabaseConnector, SubmissionConnector}
import controllers.actions.Actions
import forms.manual.account.AccountsFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.SubmissionsConstants.CRS
import models.requests.ReportIdRequest
import models.viewModels.AccountId
import models.{NormalMode, ReportId}
import pages.manual.account.{AccountsPage, AddAnotherAccountPage, CurrentAccountIdPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Status
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.ViewFIService
import services.manual.account.AccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.manual.account.AccountsSummary
import views.html.manual.account.AccountsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  repository: DatabaseConnector,
  formProvider: AccountsFormProvider,
  viewFIService: ViewFIService,
  accountsService: AccountsService,
  val controllerComponents: MessagesControllerComponents,
  view: AccountsView,
  submissionConnector: SubmissionConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def sendXml(): Action[AnyContent] = actions.withReportIdRequired().async { implicit request =>
     val payload =
       """<?xml version='1.0' encoding='UTF-8'?>
         |<crs:CRS_OECD xmlns:crs="urn:oecd:ties:crs:v3" xmlns:sfa="urn:oecd:ties:isocrstypes:v1" xmlns:cfc="urn:oecd:ties:commontypesfatcacrs:v2" xmlns:stf="urn:oecd:ties:crsstf:v5" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:oecd:ties:crs:v3 CrsXML_v3.0.xsd urn:oecd:ties:isocrstypes:v1 isocrstypes_v1.1.xsd urn:oecd:ties:crsstf:v5 oecdcrstypes_v5.0.xsd urn:oecd:ties:commontypesfatcacrs:v2 CommonTypesFatcaCrs_v2.0.xsd urn:oecd:ties:fatca:v1 FatcaTypes_v1.2.xsd" version="3.0">
         |    <crs:MessageSpec>
         |        <crs:SendingCompanyIN>TES683373339</crs:SendingCompanyIN>
         |        <crs:TransmittingCountry>GB</crs:TransmittingCountry>
         |        <crs:ReceivingCountry>MK</crs:ReceivingCountry>
         |        <crs:MessageType>CRS</crs:MessageType>
         |        <crs:Warning>a</crs:Warning>
         |        <crs:Contact>a</crs:Contact>
         |        <crs:MessageRefId>GB2013CA123456789-fastresponseaccepted</crs:MessageRefId>
         |        <crs:MessageTypeIndic>CRS701</crs:MessageTypeIndic>
         |        <crs:ReportingPeriod>2025-08-13</crs:ReportingPeriod>
         |        <crs:Timestamp>2025-12-17T09:30:47Z</crs:Timestamp>
         |    </crs:MessageSpec>
         |    <crs:CrsBody>
         |        <crs:ReportingFI>
         |            <crs:ResCountryCode>GB</crs:ResCountryCode>
         |            <crs:IN issuedBy="SX" INType="TIN">789456789</crs:IN>
         |            <crs:Name nameType="OECD202">Test FI Name</crs:Name>
         |            <crs:Address legalAddressType="OECD301">
         |                <cfc:CountryCode>HK</cfc:CountryCode>
         |                <cfc:AddressFix>
         |                    <cfc:Street>123 street</cfc:Street>
         |                    <cfc:BuildingIdentifier>Building13</cfc:BuildingIdentifier>
         |                    <cfc:SuiteIdentifier>Suite12</cfc:SuiteIdentifier>
         |                    <cfc:FloorIdentifier>floor12</cfc:FloorIdentifier>
         |                    <cfc:DistrictName>district</cfc:DistrictName>
         |                    <cfc:POB>POB1234</cfc:POB>
         |                    <cfc:PostCode>75244</cfc:PostCode>
         |                    <cfc:City>Test</cfc:City>
         |                    <cfc:CountrySubentity>TX</cfc:CountrySubentity>
         |                </cfc:AddressFix>
         |                <cfc:AddressFree>123 street/Building13/Suite12/Test/HK/75244</cfc:AddressFree>
         |            </crs:Address>
         |            <crs:DocSpec>
         |                <stf:DocTypeIndic>OECD10</stf:DocTypeIndic>
         |                <stf:DocRefId>GBFI286abc123xyz</stf:DocRefId>
         |            </crs:DocSpec>
         |        </crs:ReportingFI>
         |        <crs:ReportingGroup>
         |            <crs:AccountReport>
         |                <crs:DocSpec>
         |                    <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>
         |                    <stf:DocRefId>GBFI286abc123xyY</stf:DocRefId>
         |                </crs:DocSpec>
         |                <crs:AccountNumber AcctNumberType="OECD604" UndocumentedAccount="true" ClosedAccount="true" DormantAccount="true">AC1243246537</crs:AccountNumber>
         |                <crs:AccountHolder>
         |                    <crs:EquityInterestType>CRS406</crs:EquityInterestType>
         |                    <crs:SelfCert>CRS902</crs:SelfCert>
         |                    <crs:Organisation>
         |                        <crs:ResCountryCode>CL</crs:ResCountryCode>
         |                        <crs:IN issuedBy="BJ" INType="TIN">123125126</crs:IN>
         |                        <crs:Name nameType="OECD206">Test FI Name</crs:Name>
         |                        <crs:Address legalAddressType="OECD302">
         |                            <cfc:CountryCode>MY</cfc:CountryCode>
         |                            <cfc:AddressFix>
         |                                <cfc:Street>231 Street</cfc:Street>
         |                                <cfc:BuildingIdentifier>Building Identifier 321</cfc:BuildingIdentifier>
         |                                <cfc:SuiteIdentifier>Suite 12324</cfc:SuiteIdentifier>
         |                                <cfc:FloorIdentifier>Floor34</cfc:FloorIdentifier>
         |                                <cfc:DistrictName>district22</cfc:DistrictName>
         |                                <cfc:POB>POB345325</cfc:POB>
         |                                <cfc:PostCode>467894</cfc:PostCode>
         |                                <cfc:City>Test</cfc:City>
         |                                <cfc:CountrySubentity>AL</cfc:CountrySubentity>
         |                            </cfc:AddressFix>
         |                            <cfc:AddressFree>321 street/Building Identifier 321/Suite
         |                                123242/Test/MY/467894</cfc:AddressFree>
         |                        </crs:Address>
         |                    </crs:Organisation>
         |                    <crs:AcctHolderType>CRS103</crs:AcctHolderType>
         |                </crs:AccountHolder>
         |                <crs:ControllingPerson>
         |                    <crs:Individual>
         |                        <crs:ResCountryCode>LY</crs:ResCountryCode>
         |                        <crs:TIN issuedBy="VG">456143211</crs:TIN>
         |                        <crs:Name nameType="OECD207">
         |                            <crs:Title>MR</crs:Title>
         |                            <crs:FirstName xnlNameType="Forename">John</crs:FirstName>
         |                            <crs:MiddleName xnlNameType="middle name">Murphy</crs:MiddleName>
         |                            <crs:LastName xnlNameType="Sur Name">Hazelwood</crs:LastName>
         |                            <crs:Suffix>JR</crs:Suffix>
         |                            <crs:GeneralSuffix>JR</crs:GeneralSuffix>
         |                        </crs:Name>
         |                        <crs:Address legalAddressType="OECD302">
         |                            <cfc:CountryCode>IQ</cfc:CountryCode>
         |                            <cfc:AddressFix>
         |                                <cfc:Street>321 street</cfc:Street>
         |                                <cfc:BuildingIdentifier>Building Identifier 321</cfc:BuildingIdentifier>
         |                                <cfc:SuiteIdentifier>Suite 3242</cfc:SuiteIdentifier>
         |                                <cfc:FloorIdentifier>Floor67</cfc:FloorIdentifier>
         |                                <cfc:DistrictName>district45</cfc:DistrictName>
         |                                <cfc:POB>POB354367</cfc:POB>
         |                                <cfc:PostCode>456478</cfc:PostCode>
         |                                <cfc:City>Test</cfc:City>
         |                                <cfc:CountrySubentity>RG</cfc:CountrySubentity>
         |                            </cfc:AddressFix>
         |                            <cfc:AddressFree>321 street/Building Identifier 321/Suite
         |                                3242/Test/IQ/456478</cfc:AddressFree>
         |                        </crs:Address>
         |                        <crs:BirthInfo>
         |                            <crs:BirthDate>1957-08-13</crs:BirthDate>
         |                            <crs:City>Test</crs:City>
         |                            <crs:CitySubentity>WestMinister</crs:CitySubentity>
         |                            <crs:CountryInfo>
         |                                <crs:CountryCode>GB</crs:CountryCode>
         |                            </crs:CountryInfo>
         |                        </crs:BirthInfo>
         |                    </crs:Individual>
         |                    <crs:CtrlgPersonType>CRS812</crs:CtrlgPersonType>
         |                    <crs:SelfCert>CRS1002</crs:SelfCert>
         |                </crs:ControllingPerson>
         |                <crs:AccountBalance currCode="CAD">1230</crs:AccountBalance>
         |                <crs:Payment>
         |                    <crs:Type>CRS504</crs:Type>
         |                    <crs:PaymentAmnt currCode="XPF">3240</crs:PaymentAmnt>
         |                </crs:Payment>
         |                <crs:DDProcedure>CRS1202</crs:DDProcedure>
         |                <crs:AccountType>CRS1101</crs:AccountType>
         |                <crs:JointAccount>
         |                    <crs:Number>5</crs:Number>
         |                </crs:JointAccount>
         |            </crs:AccountReport>
         |        </crs:ReportingGroup>
         |    </crs:CrsBody>
         |</crs:CRS_OECD>
         |
         |""".stripMargin

     submissionConnector.sendXml(payload).flatMap{ result =>
       println(result)
       Future.successful(Ok)
     }.recover {
       case _ => Ok
     }
  }

  def onPageLoad(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        val preparedForm = request.userAnswers
          .get(AddAnotherAccountPage(request.reportId))
          .fold(form)(form.fill)

        renderPage(preparedForm, Ok)
    }

  def onSubmit(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        given reportId: ReportId = request.reportId

        if (!accountsService.hasAccounts(request.userAnswers, reportId)) {
          logger.warn(
            "No accounts found; redirecting to the accounts overview"
          )

          Future.successful(
            Redirect(routes.AccountsController.onPageLoad())
          )
        } else {
          form
            .bindFromRequest()
            .fold(
              formWithErrors => renderPage(formWithErrors, BadRequest),
              addAnother =>
                for {
                  updatedAnswers <- Future.fromTry(
                    request.userAnswers.setWithReportId(
                      AddAnotherAccountPage(reportId),
                      addAnother
                    )
                  )

                  destination =
                    if (addAnother) {
                      routes.AccountsController.onAddAccount()
                    } else if (accountsService.allAccountsComplete(updatedAnswers, reportId)) {
                      routes.IsAccountsSectionCompletedController.onPageLoad()
                    } else {
                      controllers.manual.routes.SendAReportController.onPageLoad()
                    }

                  _ <- repository.set(updatedAnswers)
                } yield {
                  logger.info(
                    s"Saved add-another-account answer ($addAnother); redirecting to ${destination.url}"
                  )

                  Redirect(destination)
                }
            )
        }
    }

  def onAddAccount(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        for {
          answersWithoutCurrentAccount <- Future.fromTry(
            accountsService.clearCurrentAccount(
              request.userAnswers,
              request.reportId
            )
          )
          updatedAnswers <- Future.fromTry(
            answersWithoutCurrentAccount.remove(
              AddAnotherAccountPage(request.reportId)
            )
          )
          _ <- repository.set(updatedAnswers)
        } yield {
          logger.info(
            "Cleared the previous account selection; redirecting to Have Number"
          )

          Redirect(
            routes.HaveNumberController.onPageLoad(NormalMode)
          )
        }
    }

  def onChange(accountId: String): Action[AnyContent] =
    selectAccount(
      accountId,
      request =>
        if (accountsService.isComplete(request.userAnswers, request.reportId, AccountId(accountId))) {
          routes.AccountCheckAnswersController.onPageLoad()
        } else {
          routes.HaveNumberController.onPageLoad(NormalMode)
        }
    )

  def onRemove(accountId: String): Action[AnyContent] =
    selectAccount(
      accountId,
      _ => routes.RemoveAccountController.onPageLoad()
    )

  private def selectAccount(
    accountId: String,
    destination: ReportIdRequest[AnyContent] => Call
  ): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        given reportId: ReportId = request.reportId

        val accountExists = request.userAnswers
          .get(AccountsPage(reportId))
          .exists(_.accounts.contains(accountId))

        if (!accountExists) {
          logger.warn(
            "Requested account was not found in the current report; redirecting to journey recovery"
          )

          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
        } else {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.setWithReportId(
                CurrentAccountIdPage(),
                AccountId(accountId)
              )
            )
            _ <- repository.set(updatedAnswers)
          } yield {
            val nextPage = destination(request)

            logger.info(
              s"Saved the current account selection; redirecting to ${nextPage.url}"
            )

            Redirect(nextPage)
          }
        }
    }

  private def renderPage(
    preparedForm: Form[Boolean],
    status: Status
  )(implicit request: ReportIdRequest[AnyContent]): Future[Result] = {

    val accounts = accountsService.orderedAccounts(
      request.userAnswers,
      request.reportId
    )

    viewFIService
      .getFIDetail(request.fatcaId, request.reportId.fiId)
      .map {
        fiDetail =>
          val summaryList = AccountsSummary(
            accounts = accounts,
            isComplete = accountId =>
              accountsService.isComplete(
                request.userAnswers,
                request.reportId,
                accountId
              ),
            changeAction = accountId => routes.AccountsController.onChange(accountId.value),
            removeAction = accountId => routes.AccountsController.onRemove(accountId.value)
          )

          status(
            view(
              form = preparedForm,
              summaryList = summaryList,
              fiName = fiDetail.FIName,
              isCrs = request.reportId.regime == CRS,
              submitAction = routes.AccountsController.onSubmit(),
              addAccountAction = routes.AccountsController.onAddAccount(),
              sendAReportAction = controllers.manual.routes.SendAReportController.onPageLoad()
            )
          )
      }
      .recover {
        case NoFiDetailFound =>
          logger.error(
            "Financial institution details were not found; redirecting to journey recovery"
          )

          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
      }
  }
}
