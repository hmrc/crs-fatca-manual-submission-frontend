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

package pages.manual.cpso

import base.SpecBase
import models.response.{Address, Country}
import models.viewModels.manual.cpso.CPSOId
import models.{AddressNonUk, ReportId, UkAddress}

class SelectAddressPageSpec extends SpecBase {

  private val cpsoId: CPSOId = CPSOId("1")

  implicit private val reportId: ReportId = ReportId(models.SubmissionsConstants.CRS, 2023, None, "test1")

  private val address: Address =
    Address(None, "line 1", None, None, None, "town", Some("ZZ1 1ZZ"), Country.GB)

  private val nonUkAddress: AddressNonUk =
    AddressNonUk("line 1", None, "town", None, Some("12345"), "US")

  private val ukAddress: UkAddress =
    UkAddress("line 1", None, "town", None, "ZZ1 1ZZ", "GB")

  "cleanupWithReportId" - {

    "must remove all competing address answers when an address is selected" in {
      val userAnswers = emptyUserAnswers
        .withPage(UkPostCodePage(cpsoId, reportId), "ZZ1 1ZZ")
        .withPage(AddressUkPage(cpsoId, reportId), ukAddress)
        .withPage(CPSOAddressNonUkPage(cpsoId, reportId), nonUkAddress)
        .withPage(WhatIsAddressPage(cpsoId, reportId), address)

      val result = SelectAddressPage(cpsoId, reportId)
        .cleanupWithReportId(Some(address), userAnswers)
        .success
        .value

      mustBeRemoved(
        result,
        UkPostCodePage(cpsoId, reportId),
        AddressUkPage(cpsoId, reportId),
        CPSOAddressNonUkPage(cpsoId, reportId),
        WhatIsAddressPage(cpsoId, reportId)
      )
    }

    "must preserve competing addresses when the selected address is removed" in {
      val userAnswers = emptyUserAnswers
        .withPage(UkPostCodePage(cpsoId, reportId), "ZZ1 1ZZ")
        .withPage(CPSOAddressNonUkPage(cpsoId, reportId), nonUkAddress)

      val result = SelectAddressPage(cpsoId, reportId)
        .cleanupWithReportId(None, userAnswers)
        .success
        .value

      mustBeUnaffected(
        result,
        UkPostCodePage(cpsoId, reportId),
        CPSOAddressNonUkPage(cpsoId, reportId)
      )
    }
  }
}
