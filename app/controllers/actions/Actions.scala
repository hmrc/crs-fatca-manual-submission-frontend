/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.actions

import models.requests.{
  AccountHolderIdRequest,
  AccountIdRequest,
  AccountPaymentIndexRequest,
  CPSOIdRequest,
  ReportIdRequest,
  SponsorNameRequest,
  SponsorTaxResidentIdRequest
}
import play.api.mvc.{ActionBuilder, AnyContent}

import javax.inject.Inject

class Actions @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  accountIdCreationAction: AccountIdCreationAction,
  accountIdRequiredAction: AccountIdRequiredAction,
  accountHolderIdRequiredAction: AccountHolderIdRequiredAction,
  sponsorNameRequiredAction: SponsorNameRequiredAction,
  cpsoIdCreationAction: CpsoIdCreationAction,
  taxResidentCountryIdCreationAction: TaxResidentCountryIdCreationAction,
  accountPaymentIdCreationAction: AccountPaymentIdCreationAction,
  accountPaymentIndexRequiredAction: AccountPaymentIndexRequiredAction
) {

  def withReportIdRequiredAndAccountIdCreation(): ActionBuilder[AccountIdRequest, AnyContent] =
    withReportIdRequired() andThen accountIdCreationAction

  def withReportIdRequiredAndAccountIdRequired(): ActionBuilder[AccountIdRequest, AnyContent] =
    withReportIdRequired() andThen accountIdRequiredAction

  def withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIdCreation(): ActionBuilder[AccountPaymentIndexRequest, AnyContent] =
    withReportIdRequired() andThen accountIdRequiredAction andThen accountPaymentIdCreationAction

  def withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexRequired(): ActionBuilder[AccountPaymentIndexRequest, AnyContent] =
    withReportIdRequired() andThen accountIdRequiredAction andThen accountPaymentIndexRequiredAction

  def withReportIdRequiredAndAccountHolderIdRequired(): ActionBuilder[AccountHolderIdRequest, AnyContent] =
    withReportIdRequired() andThen accountHolderIdRequiredAction

  def withReportIdRequiredAndCPSOIdCreation(): ActionBuilder[CPSOIdRequest, AnyContent] =
    withReportIdRequired() andThen cpsoIdCreationAction

  def withReportIdRequiredAndSponsorNameRequired(): ActionBuilder[SponsorNameRequest, AnyContent] =
    withReportIdRequired() andThen sponsorNameRequiredAction

  def withReportIdRequiredAndSponsorNameRequiredAndIdCreation(): ActionBuilder[SponsorTaxResidentIdRequest, AnyContent] =
    withReportIdRequiredAndSponsorNameRequired() andThen taxResidentCountryIdCreationAction

  def withReportIdRequired(): ActionBuilder[ReportIdRequest, AnyContent] =
    identityAndRequireData andThen reportIdAction

  private def identityAndRequireData = identify andThen getData andThen requireData
}
