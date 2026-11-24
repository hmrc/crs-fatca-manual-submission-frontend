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

package models.manual

import models.manual.account.{AccountNumber, PaymentType, WasAccountOpen, WhatAccountType}
import models.{AccountBalance, NumberType}

sealed trait AccountType {
  def accountNumber: String
  def payments: List[Payment]
  def isClosed: Boolean
  def balance: AccountBalance
}

object AccountType {

  case class CrsAccount(numberType: Option[NumberType],
                        accountIdentifier: AccountNumber,
                        isUndocumented: Boolean,
                        isDormant: Boolean,
                        isClosed: Boolean,
                        wasOpenInRP: WasAccountOpen,
                        accountBalance: AccountBalance,
                        howManyJointHolders: Option[Int],
                        accountType: WhatAccountType,
                        payments: List[Payment]
  ) extends AccountType {
    val accountNumber: String = accountIdentifier.id
    override val balance: AccountBalance = if isClosed then accountBalance.copy(amount = "0.00") else accountBalance
  }

  case class FatcaAccount(numberType: Option[NumberType],
                          accountIdentifier: AccountNumber,
                          accountBalance: AccountBalance,
                          isClosed: Boolean,
                          payments: List[Payment]) extends AccountType {
    val accountNumber: String = accountIdentifier.id
    override val balance: AccountBalance  = if isClosed then accountBalance.copy(amount = "0.00") else accountBalance
  }
}
