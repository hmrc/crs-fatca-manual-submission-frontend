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
      case (accountHolderType, expected) =>
        s"must return $expected when the CRS organisation type is ${accountHolderType.getOrElse("not set")}" in {
          AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
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
      case (accountHolderType, expected) =>
        s"must return $expected when the FATCA organisation type is ${accountHolderType.getOrElse("not set")}" in {
          AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
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
              individualOrOrganisation = Some(Individual)
            ).canHaveCPSO() mustBe false
          }
      }
    }

    "must be false when the individual or organisation question has not been answered" - {

      "Even when an eligible organisation type is present" in {
        AccountHolder(
          individualOrOrganisation = None,
          accountHolderType = Some("FATCA101")
        ).canHaveCPSO() mustBe false
      }

      "when no account holder type type is set" in {
        AccountHolder().canHaveCPSO() mustBe false
        AccountHolder().canHaveCPSO() mustBe false
      }
    }

    "for a CRS report" - {

      "must read the CRS account holder type" - {

        Seq(
          Some("CRS101") -> true,
          Some("CRS100") -> false,
          Some("CRS102") -> false,
          Some("CRS103") -> false,
          Some("")       -> false,
          None           -> false
        ).foreach {
          case (accountHolderType, expected) =>
            s"must return $expected when the CRS organisation type is ${accountHolderType.getOrElse("not set")}" in {
              AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
                .canHaveCPSO() mustBe expected
            }
        }
      }
    }

    "for a FATCA report" - {

      "must read the FATCA accountHolder Type" - {

        Seq(
          Some("FATCA101") -> true,
          Some("FATCA102") -> true,
          Some("FATCA100") -> false,
          Some("FATCA103") -> false,
          Some("")         -> false,
          None             -> false
        ).foreach {
          case (accountHolderType, expected) =>
            s"must return $expected when the FATCA account holder type is ${accountHolderType.getOrElse("not set")}" in {
              AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
                .canHaveCPSO() mustBe expected
            }
        }
      }
    }

  }
}
