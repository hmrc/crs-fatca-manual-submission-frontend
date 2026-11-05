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

package viewmodels.manual.accountHolders

import models.SubmissionsConstants.RegimeType
import models.viewModels.{AccountHolder, AccountHolderId}

case class AccountHolderRow(accountHolderName: String, changeUrl: String, removeUrl: String, isIncomplete: Boolean = false)

object AccountHolderRow {

  def rows(accountInfoList: Seq[(AccountHolderId, AccountHolder)], regimeType: RegimeType): Seq[AccountHolderRow] =
    accountInfoList.map {
      case (accountHolderId, acountHolder) =>
        AccountHolderRow(
          acountHolder.organisationName.getOrElse(acountHolder.getName()),
          controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onChangeRedirect(accountHolderId.value).url,
          controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onRemoveRedirect(accountHolderId.value).url
        )
    }
}
