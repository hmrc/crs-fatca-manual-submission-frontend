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

package generators

import models.{CrsOrFatca, TypeOfReport}
import org.scalacheck.{Arbitrary, Gen}

import models.UkAddress
import org.scalacheck.Arbitrary.*

trait ModelGenerators {

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[String]
        city         <- arbitrary[String]
        county       <- arbitrary[Option[String]]
        postcode     <- arbitrary[String]
        country      <- arbitrary[String]
      } yield UkAddress(addressLine1, Some(addressLine2), city, county, postcode, country)
    }

  implicit lazy val arbitraryTypeOfReport: Arbitrary[TypeOfReport] =
    Arbitrary {
      Gen.oneOf(TypeOfReport.values)
    }

  implicit lazy val arbitraryCrsOrFatca: Arbitrary[CrsOrFatca] =
    Arbitrary {
      Gen.oneOf(CrsOrFatca.values)
    }
}
