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

package forms.manual.accountHolders

import forms.mappings.Mappings
import models.Enumerable
import models.viewModels.AccountId
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class AccountFormProvider @Inject() extends Mappings {

  def apply(accountIds: Seq[AccountId]): Form[Set[AccountId]] = {

    implicit val accountIdEnumerable: Enumerable[AccountId] =
      Enumerable(
        accountIds.map(
          accountId => accountId.value -> accountId
        ): _*
      )

    Form(
      "value" ->
        set(
          enumerable("accountHolder.account.error.required")
        ).verifying(
          nonEmptySet("accountHolder.account.error.required")
        )
    )
  }
}
