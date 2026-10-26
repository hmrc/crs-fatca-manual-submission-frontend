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

package models.viewModels

import models.SubmissionsConstants.{CRFA, CRS, FATCA, RegimeType}
import models.manual.accountHolders.IndividualOrOrganisation.Individual
import models.manual.accountHolders.IndividualOrOrganisation.Organisation
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AccountHolderSpec extends AnyFreeSpec with Matchers {

  "AccountHolder.cpsoIsMandatory" - {
    Seq(
      Some("CRS101") -> true,
      Some("CRS100") -> false,
      Some("CRS102") -> false,
      Some("CRS103") -> false,
      Some("")       -> false,
      None           -> false
    ).foreach {
      case (organisationType, expected) =>
        s"must return $expected when the CRS organisation type is ${organisationType.getOrElse("not set")}" in {
          AccountHolder(individualOrOrganisation = Some(Organisation), organisationCrs = organisationType)
            .cpsoIsMandatory(CRS) mustBe expected
        }
    }

    Seq(
      Some("FATCA101") -> false,
      Some("FATCA102") -> false,
      Some("FATCA103") -> false,
      Some("FATCA104") -> false,
      Some("")         -> false,
      None             -> false
    ).foreach {
      case (organisationType, expected) =>
        s"must return $expected when the FATCA organisation type is ${organisationType.getOrElse("not set")}" in {
          AccountHolder(individualOrOrganisation = Some(Organisation), organisationFatca = organisationType)
            .cpsoIsMandatory(CRS) mustBe expected
        }
    }

    "must return false for an individual account holder" in {
      AccountHolder(individualOrOrganisation = Some(Individual))
        .cpsoIsMandatory(CRS) mustBe false
    }

  }

  "AccountHolder.canHaveCPSO" - {

    "must be false for an individual account holder" - {

      Seq[RegimeType](CRS, FATCA, CRFA).foreach {
        regime =>
          s"for the ${regime.value} regime, even when an eligible organisation type is present" in {
            AccountHolder(
              individualOrOrganisation = Some(Individual),
              organisationFatca = Some("FATCA101"),
              organisationCrs = Some("CRS101")
            ).canHaveCPSO(regime) mustBe false
          }
      }
    }

    "must be false when the individual or organisation question has not been answered" - {

      Seq[RegimeType](CRS, FATCA, CRFA).foreach {
        regime =>
          s"for the ${regime.value} regime, even when an eligible organisation type is present" in {
            AccountHolder(
              individualOrOrganisation = None,
              organisationFatca = Some("FATCA101"),
              organisationCrs = Some("CRS101")
            ).canHaveCPSO(regime) mustBe false
          }
      }

      "when no organisation type is set" in {
        AccountHolder().canHaveCPSO(CRS) mustBe false
        AccountHolder().canHaveCPSO(FATCA) mustBe false
      }
    }

    "for a CRS report" - {

      "must read the CRS organisation type" - {

        Seq(
          Some("CRS101") -> true,
          Some("CRS100") -> false,
          Some("CRS102") -> false,
          Some("CRS103") -> false,
          Some("")       -> false,
          None           -> false
        ).foreach {
          case (organisationType, expected) =>
            s"must return $expected when the CRS organisation type is ${organisationType.getOrElse("not set")}" in {
              AccountHolder(individualOrOrganisation = Some(Organisation), organisationCrs = organisationType)
                .canHaveCPSO(CRS) mustBe expected
            }
        }
      }

      "must ignore the FATCA organisation type" in {
        AccountHolder(individualOrOrganisation = Some(Organisation), organisationFatca = Some("FATCA101"))
          .canHaveCPSO(CRS) mustBe false
      }
    }

    "for a FATCA report" - {

      "must read the FATCA organisation type" - {

        Seq(
          Some("FATCA101") -> true,
          Some("FATCA102") -> true,
          Some("FATCA100") -> false,
          Some("FATCA103") -> false,
          Some("")         -> false,
          None             -> false
        ).foreach {
          case (organisationType, expected) =>
            s"must return $expected when the FATCA organisation type is ${organisationType.getOrElse("not set")}" in {
              AccountHolder(individualOrOrganisation = Some(Organisation), organisationFatca = organisationType)
                .canHaveCPSO(FATCA) mustBe expected
            }
        }
      }

      "must ignore the CRS organisation type" in {
        AccountHolder(individualOrOrganisation = Some(Organisation), organisationCrs = Some("CRS101"))
          .canHaveCPSO(FATCA) mustBe false
      }
    }

  }
}
