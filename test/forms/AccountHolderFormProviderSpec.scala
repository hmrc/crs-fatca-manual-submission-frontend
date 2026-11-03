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
import models.viewModels.AccountHolderId
import play.api.data.FormError

class AccountHolderFormProviderSpec extends CheckboxFieldBehaviours {

  private val accountHolderIds = Seq(
    AccountHolderId("1"),
    AccountHolderId("2"),
    AccountHolderId("3")
  )

  private val form = new AccountHolderFormProvider()(accountHolderIds)

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "cpso.accountHolder.error.required"

    accountHolderIds.zipWithIndex.foreach {
      case (accountHolderId, index) =>
        s"must bind `${accountHolderId.value}` successfully" in {
          val result = form.bind(Map(s"$fieldName[$index]" -> accountHolderId.value))

          result.get mustEqual Set(accountHolderId)
          result.errors mustBe empty
        }
    }

    "must bind several account holders at once" in {
      val result = form.bind(
        Map(
          s"$fieldName[0]" -> accountHolderIds.head.value,
          s"$fieldName[1]" -> accountHolderIds(1).value
        )
      )

      result.get mustEqual Set(accountHolderIds.head, accountHolderIds(1))
      result.errors mustBe empty
    }

    "must fail to bind when the answer is invalid" in {
      form.bind(Map(s"$fieldName[0]" -> "invalid value")).errors must contain(
        FormError(s"$fieldName[0]", "error.invalid")
      )
    }

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
