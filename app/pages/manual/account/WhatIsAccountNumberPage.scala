package pages.manual.account

import models.ReportId
import play.api.libs.json.JsPath

final case class WhatIsAccountNumberPage(accountId: AccountId, reportId: ReportId) extends QuestionPage[String]:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "accounts" \ accountId.value \ "whatIsAccountNumber"
