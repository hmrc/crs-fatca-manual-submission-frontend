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

import models.manual.filercategory.*
import models.{CrsOrFatca, TypeOfReport}
import org.scalacheck.{Arbitrary, Gen}

import models.NumberType

trait ModelGenerators {

  implicit lazy val arbitraryWhatTypeOfFilerIsSponsor: Arbitrary[WhatTypeOfFilerIsSponsor] =
    Arbitrary {
      Gen.oneOf(WhatTypeOfFilerIsSponsor.values)
    }

  implicit lazy val arbitraryWhatTypeOfFiler: Arbitrary[WhatTypeOfFiler] =
    Arbitrary {
      Gen.oneOf(WhatTypeOfFiler.values.toSeq)
    }

  implicit lazy val arbitraryNumberType: Arbitrary[NumberType] =
    Arbitrary {
      Gen.oneOf(NumberType.values.toSeq)
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
