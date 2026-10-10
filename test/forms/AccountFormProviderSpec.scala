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

package forms

import forms.behaviours.CheckboxFieldBehaviours
import forms.manual.accountHolders.AccountFormProvider
import models.viewModels.AccountId
import play.api.data.FormError

class AccountFormProviderSpec extends CheckboxFieldBehaviours {

  private val accountIds = Seq(
    AccountId("1"),
    AccountId("2"),
    AccountId("3")
  )

  val form = new AccountFormProvider()(accountIds)

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "accountHolder.account.error.required"

    behave like checkboxField(
      form,
      fieldName,
      validValues = accountIds,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
