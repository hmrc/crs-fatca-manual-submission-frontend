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

package models.manual.accountHolders

import models.{Enumerable, WithName}

sealed trait AccountHolderTypes

object AccountHolderTypes extends Enumerable.Implicits {

  case object PassiveNonFinancialWithOneOrMoreControllingPerson extends WithName("CRS101") with AccountHolderTypes
  case object CrsReportablePerson extends WithName("CRS102") with AccountHolderTypes
  case object PassiveNonFinancialEntityThatIsCRSReportable extends WithName("CRS103") with AccountHolderTypes

  case object OwnerDocumentedForeignFinancialInstitution extends WithName("FATCA101") with AccountHolderTypes
  case object PassiveNFFEWithSubstantial extends WithName("FATCA102") with AccountHolderTypes
  case object NonParticipatingForeignFI extends WithName("FATCA103") with AccountHolderTypes
  case object SpecifiedUSPerson extends WithName("FATCA104") with AccountHolderTypes

  val values: Seq[AccountHolderTypes] = Seq(
    PassiveNonFinancialWithOneOrMoreControllingPerson,
    CrsReportablePerson,
    PassiveNonFinancialEntityThatIsCRSReportable,
    OwnerDocumentedForeignFinancialInstitution,
    PassiveNFFEWithSubstantial,
    NonParticipatingForeignFI,
    SpecifiedUSPerson
  )

  implicit val enumerable: Enumerable[AccountHolderTypes] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
