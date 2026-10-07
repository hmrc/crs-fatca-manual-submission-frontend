package pages.manual.accountHolders

import models.ReportId
import models.response.Country
import models.viewModels.AccountHolderId
import pages.QuestionPage
import play.api.libs.json.JsPath

final case class ResidentTaxPage(currentId: AccountHolderId, reportId: ReportId) extends QuestionPage[Country]:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "residentTax"
