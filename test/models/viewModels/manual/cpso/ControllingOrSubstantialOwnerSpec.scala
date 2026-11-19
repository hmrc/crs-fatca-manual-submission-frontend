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

package models.viewModels.manual.cpso

import base.SpecBase
import models.SubmissionsConstants.CRS
import models.manual.TaxResidentInformation
import models.manual.accountHolders.IndividualDateOfBirth
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes, CpsoSelfCertification, IndividualName}
import models.{AddressNonUk, UkAddress}
import models.response.{Address, Country}
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class ControllingOrSubstantialOwnerSpec extends SpecBase {

  "IsComplete" - {
    val controllingPersonTypes = ControllingPersonTypes(Seq(ControllingPersonType.OwnershipOfLegalPerson))
    val accountHolders         = Seq("ah-1")
    val individualDateOfBirth  = IndividualDateOfBirth(LocalDate.of(2000, 10, 12))
    val individualName         = IndividualName("First Name", "Last Name")
    val ukAddressSelect        = Address(None, "addressLine1", None, None, None, "town", None, Country.GB)
    val ukAddressManual        = UkAddress("Some Address", Some("Some Address"), "City", Some("Some Address"), "postcode: ", "coutry")
    val addressNonUk           = AddressNonUk(addressLine1 = "some address", None, "", None, None, "GB")
    val taxResidentInformation = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = Some(true), taxIdentifierNumber = Some("12333"))

    "CRS regime" - {

      val baseControllingPerson = ControllingOrSubstantialOwner(
        individualName = Some(individualName),
        accountHolders = Some(accountHolders),
        individualHaveDateOfBirth = Some(false),
        individualHavePlaceOfBirth = Some(false),
        areTheyBasedInTheUk = Some(false),
        taxResidentCountries = Some(Seq(taxResidentInformation)),
        addressNonUk = Some(addressNonUk),
        selfCertification = Some(CpsoSelfCertification.Yes),
        controllingPersonType = Some(controllingPersonTypes)
      )

      "is complete for a non-uk address controlling person that has all required fields completed" in {
        baseControllingPerson.isComplete(CRS) mustBe true
      }
    }

  }

}
